package com.coreoz.plume.mfa.authenticator.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.coreoz.plume.mfa.authenticator.db.daos.MfaAuthenticatorDao;
import com.coreoz.plume.mfa.authenticator.db.generated.MfaAuthenticator;
import com.coreoz.plume.mfa.authenticator.errors.MfaError;
import com.coreoz.plume.mfa.authenticator.errors.MfaException;
import com.coreoz.plume.mfa.authenticator.services.encryption.MfaSecretKeyEncryptionProvider;
import com.coreoz.plume.mfa.authenticator.webservices.data.UserCredentials;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Singleton
public class MfaAuthenticatorService {

    private static final Logger logger = LoggerFactory.getLogger(MfaAuthenticatorService.class);

    private final GoogleAuthenticator authenticator = new GoogleAuthenticator();
    private final MfaSecretKeyEncryptionProvider mfaSecretKeyEncryptionProvider;
    private final MfaConfigurationService configurationService;
    private final MfaUserService userService;
    private final MfaAuthenticatorDao mfaAuthenticatorDao;

    @Inject
    private MfaAuthenticatorService(
        MfaSecretKeyEncryptionProvider mfaSecretKeyEncryptionProvider,
        MfaUserService userService,
        MfaAuthenticatorDao mfaAuthenticatorDao,
        MfaConfigurationService configurationService
    ) {
        this.mfaSecretKeyEncryptionProvider = mfaSecretKeyEncryptionProvider;
        this.configurationService = configurationService;
        this.mfaAuthenticatorDao = mfaAuthenticatorDao;
        this.userService = userService;
    }

    /**
     * Generate a new secret key for a MFA authenticator
     * The secret key is not hashed
     * @return
     * @throws Exception
     */
    public String generateSecretKey() throws MfaException {
        try {
            GoogleAuthenticatorKey key = authenticator.createCredentials();
            return key.getKey();
        } catch (Exception e) {
            logger.error("Error generating secret key", e);
            throw new MfaException(MfaError.GENERATE_KEY_ERROR);
        }
    }

    public String hashSecretKey(String secretKey) throws MfaException {
        try {
            return mfaSecretKeyEncryptionProvider.get().encrypt(secretKey);
        } catch (Exception e) {
            logger.error("Error hashing secret key", e);
            throw new MfaException(MfaError.ENCRYPTION_ERROR);
        }
    }

    public String getQRBarcodeURL(UserCredentials user, String secret) {
        final String issuer = configurationService.appName();
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, user.getUserName(), secret, issuer);
    }

    public byte[] generateQRCode(UserCredentials user, String secret) {
        String qrBarcodeURL = getQRBarcodeURL(user, secret);
        try {
            return QRCodeGenerator.generateQRCodeImage(qrBarcodeURL, 200, 200);
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code", e);
            return null;
        }
    }

    private boolean verifyCode(String secret, int code) {
        try {
            return authenticator.authorize(mfaSecretKeyEncryptionProvider.get().decrypt(secret), code);
        } catch (Exception e) {
            logger.info("could not decrypt secret key", e);
            return false;
        }
    }

    /**
     * Create a new MFA authenticator secret key for a user
     * The secret key is stored hashed in the database and the plain secret key is returned
     * @param credentials
     * @return
     * @throws Exception
     */
    public String createMfaAuthenticatorSecretKey(UserCredentials credentials) throws MfaException {
        Long idUser = userService.authenticatedUserId(credentials.getUserName(), credentials.getPassword());
        if (idUser == null) {
            throw new MfaException(MfaError.NO_USER_FOUND);
        }
        String secretKey = generateSecretKey();
        MfaAuthenticator mfa = new MfaAuthenticator();
        mfa.setSecretKey(hashSecretKey(secretKey));
        mfa.setIdUser(idUser);
        mfa.setIsEnabled(false);
        mfa.setCreationDate(Instant.now());
        mfaAuthenticatorDao.save(mfa);

        return secretKey;
    }

    private MfaAuthenticator findValidAuthenticator(String username, int code) {
        Long idUser = userService.userId(username);
        List<MfaAuthenticator> authenticators = mfaAuthenticatorDao.findByIdUser(idUser);
        return authenticators.stream().filter(mfa -> verifyCode(mfa.getSecretKey(), code)).findFirst().orElse(null);
    }

    /**
     * Check if the authenticator code is valid for a user
     * @param username
     * @param code
     * @return
     */
    public boolean isCredentialsValidForUser(String username, int code) {
        MfaAuthenticator validAuthenticator = findValidAuthenticator(username, code);
        if (validAuthenticator != null) {
            validAuthenticator.setIsEnabled(true);
            validAuthenticator.setLastUsedDate(Instant.now());
            return true;
        }
        return false;
    }

    /**
     * List all the authenticators for a user, can be used to show the user
     * which authenticators are enabled and delete them if needed
     * @param username
     * @return
     */
    public List<MfaAuthenticator> listAuthenticators(String username) {
        Long idUser = userService.userId(username);
        return mfaAuthenticatorDao.findByIdUser(idUser);
    }

    private static class QRCodeGenerator {
        public static byte[] generateQRCodeImage(String barcodeText, int width, int height) throws WriterException, IOException {
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        }
    }

    /**
     * Delete an authenticator by its id
     * @param id
     */
    public void deleteAuthenticator(long id) {
        mfaAuthenticatorDao.delete(id);
    }

    /**
     * delete all not enabled authenticator with a creation date older than the given date
     * @param date the date to compare the creation date with
     */
    public void deleteOldNotEnabledAuthenticators(Instant date) {
        mfaAuthenticatorDao.findDisabledAndOlderThan(date)
            .forEach(authenticator -> deleteAuthenticator(authenticator.getId()));
    }
}
