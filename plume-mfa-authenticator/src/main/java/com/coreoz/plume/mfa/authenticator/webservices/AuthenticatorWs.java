package com.coreoz.plume.mfa.authenticator.webservices;

import javax.inject.Inject;
import javax.inject.Singleton;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coreoz.plume.jersey.errors.WsError;
import com.coreoz.plume.jersey.errors.WsException;
import com.coreoz.plume.jersey.security.basic.Credentials;
import com.coreoz.plume.jersey.security.permission.PublicApi;
import com.coreoz.plume.mfa.authenticator.services.MfaAuthenticatorService;
import com.coreoz.plume.mfa.authenticator.webservices.data.AuthenticatorCredentials;
import com.coreoz.plume.mfa.authenticator.webservices.data.MfaQrcode;

@Path("/auhenticator")
@Tag(name = "admin-session", description = "Manage the administration session")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
// This API is marked as public, since it must be accessed without any authentication
@PublicApi
@Singleton
public class AuthenticatorWs {
	private final Logger logger = LoggerFactory.getLogger(AuthenticatorWs.class);
	private final MfaAuthenticatorService mfaAuthenticatorService;

	@Inject
	public AuthenticatorWs(MfaAuthenticatorService mfaAuthenticatorService) {
		this.mfaAuthenticatorService = mfaAuthenticatorService;
	}

	@POST
	@Operation(description = "Generate a qrcode for MFA enrollment")
    @Path("/qrcode-url")
	public MfaQrcode qrCodeUrl(Credentials credentials) {
        // Generate MFA secret key and QR code URL
        try {
            String secretKey = mfaAuthenticatorService.createMfaAuthenticatorSecretKey(credentials);
            String qrCodeUrl = mfaAuthenticatorService.getQRBarcodeURL(credentials, secretKey);

            // Return the QR code URL to the client
            return new MfaQrcode(qrCodeUrl);
        } catch (Exception e) {
            logger.debug("erreur lors de la génération du QR code", e);
			// TODO: Delete the secret key from the database
            throw new WsException(WsError.INTERNAL_ERROR);
        }
	}

    @POST
	@Operation(description = "Generate a qrcode for MFA enrollment")
    @Path("/qrcode")
	public Response qrCode(Credentials credentials) {
		// First user needs to be authenticated (an exception will be raised otherwise)

        // Generate MFA secret key and QR code URL
        try {
            String secretKey = mfaAuthenticatorService.createMfaAuthenticatorSecretKey(credentials);
            byte[] qrCode = mfaAuthenticatorService.generateQRCode(credentials, secretKey);

            // Return the QR code image to the client
            ResponseBuilder response = Response.ok(qrCode);
            response.header("Content-Disposition", "attachment; filename=qrcode.png");
            response.header("Content-Type", "image/png");
            return response.build();
        } catch (Exception e) {
            logger.debug("erreur lors de la génération du QR code", e);
            throw new WsException(WsError.INTERNAL_ERROR);
        }
	}

    @POST
    @Path("/verify")
    @Operation(description = "Verify MFA code for authentication")
    public Response verifyMfa(AuthenticatorCredentials credentials) {
        return Response.ok().build();
    }
}
