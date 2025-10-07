package com.example.shamir;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.Sign.SignatureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/api")
public class WalletController {

    @Autowired
    private WalletService walletService;
    
    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    @PostMapping("/generateWallet")
    public ResponseEntity<String> generateWallet(@RequestParam String password) {
        logger.debug("Generating wallet with password: {}", password);
        try {
            String walletDetails = walletService.generateWallet(password);
            logger.debug("Wallet generated: {}", walletDetails);
            return ResponseEntity.ok("Wallet generated successfully. " + walletDetails);
        } catch (Exception e) {
            logger.error("Failed to generate wallet", e);
            return ResponseEntity.badRequest().body("Failed to generate wallet: " + e.getMessage());
        }
    }

    @PostMapping("/splitPrivateKey")
    public ResponseEntity<Map<Integer, String>> splitPrivateKey(@RequestParam String privateKey, @RequestParam int n, @RequestParam int k) {
        logger.debug("Splitting private key: {} into {} parts with threshold {}", privateKey, n, k);
        try {
            Map<Integer, String> shares = walletService.splitPrivateKey(privateKey, n, k);
            logger.debug("Private key split into shares: {}", shares);
            return ResponseEntity.ok(shares);
        } catch (Exception e) {
            logger.error("Failed to split private key", e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/recoverPrivateKey")
    public ResponseEntity<String> recoverPrivateKey(@RequestBody Map<Integer, String> shares) {
        logger.debug("Recovering private key from shares: {}", shares);
        try {
            String privateKey = walletService.recoverPrivateKey(shares);
            logger.debug("Private key recovered: {}", privateKey);
            return ResponseEntity.ok("Recovered Private Key: " + privateKey);
        } catch (Exception e) {
            logger.error("Failed to recover private key", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to recover private key: " + e.getMessage());
        }
    }

    @PostMapping("/signMessage")
    public ResponseEntity<SignatureData> signMessage(@RequestParam String privateKey, @RequestParam String message) {
        logger.debug("Signing message: {} with private key: {}", message, privateKey);
        try {
            SignatureData signature = walletService.signMessage(privateKey, message);
            logger.debug("Message signed: {}", signature);
            return ResponseEntity.ok(signature);
        } catch (Exception e) {
            logger.error("Failed to sign message", e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/verifySignature")
    public ResponseEntity<Map<String, Object>> verifySignature(@RequestBody Map<String, Object> payload) {
        String message = (String) payload.get("message");
        Map<String, String> signatureMap = (Map<String, String>) payload.get("signature");
        String publicKey = (String) payload.get("publicKey");

        SignatureData signatureData = new SignatureData(
                Base64.getDecoder().decode(signatureMap.get("v"))[0],
                Base64.getDecoder().decode(signatureMap.get("r")),
                Base64.getDecoder().decode(signatureMap.get("s"))
        );

        logger.debug("Verifying signature for message: {} with public key: {}", message, publicKey);
        try {
            boolean isVerified = walletService.verifySignature(message, signatureData, publicKey);
            logger.debug("Signature verification result: {}", isVerified);
            Map<String, Object> response = new HashMap<>();
            response.put("isVerified", isVerified);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to verify signature", e);
            Map<String, Object> response = new HashMap<>();
            response.put("isVerified", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}
