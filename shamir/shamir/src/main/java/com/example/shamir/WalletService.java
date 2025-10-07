package com.example.shamir;

import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Sign.SignatureData;

import com.codahale.shamir.Scheme;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WalletService {
    private static final String WALLET_PATH = "src/main/resources/wallets";
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    public String generateWallet(String password) throws Exception {
        logger.debug("Generating wallet with password: {}", password);
        File walletDirectory = new File(WALLET_PATH);
        if (!walletDirectory.exists()) {
            if (!walletDirectory.mkdirs()) {
                throw new Exception("Unable to create wallet directory at " + WALLET_PATH);
            }
        }

        Bip39Wallet wallet = WalletUtils.generateBip39Wallet(password, walletDirectory);
        Credentials credentials = WalletUtils.loadBip39Credentials(password, wallet.getMnemonic());
        String walletDetails = "PublicKey: " + credentials.getEcKeyPair().getPublicKey().toString(16) + 
                               ", PrivateKey: " + credentials.getEcKeyPair().getPrivateKey().toString(16);
        logger.debug("Wallet generated: {}", walletDetails);
        return walletDetails;
    }

    public Map<Integer, String> splitPrivateKey(String privateKey, int n, int k) {
        logger.debug("Splitting private key: {} into {} parts with threshold {}", privateKey, n, k);
        Scheme scheme = new Scheme(new SecureRandom(), n, k);
        byte[] secret = new BigInteger(privateKey, 16).toByteArray();
        Map<Integer, byte[]> shares = scheme.split(secret);
        Map<Integer, String> encodedShares = new HashMap<>();
        shares.forEach((id, share) -> encodedShares.put(id, Base64.getEncoder().encodeToString(share)));
        logger.debug("Private key split into shares: {}", encodedShares);
        return encodedShares;
    }

    public String recoverPrivateKey(Map<Integer, String> shares) {
        logger.debug("Recovering private key from shares: {}", shares);
        Scheme scheme = new Scheme(new SecureRandom(), shares.size(), shares.size() / 2 + 1);
        Map<Integer, byte[]> decodedShares = new HashMap<>();
        shares.forEach((id, share) -> decodedShares.put(id, Base64.getDecoder().decode(share)));
        byte[] recovered = scheme.join(decodedShares);
        String privateKey = new BigInteger(recovered).toString(16);
        logger.debug("Private key recovered: {}", privateKey);
        return privateKey;
    }

    public SignatureData signMessage(String privateKeyHex, String message) throws Exception {
        logger.debug("Signing message: {} with private key: {}", message, privateKeyHex);
        BigInteger privateKey = new BigInteger(privateKeyHex, 16);
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        SignatureData signature = Sign.signMessage(messageBytes, keyPair);
        logger.debug("Message signed: {}", signature);
        return signature;
    }

    public boolean verifySignature(String message, SignatureData signature, String publicKeyHex) throws Exception {
        logger.debug("Verifying signature for message: {} with public key: {}", message, publicKeyHex);
        BigInteger publicKey = new BigInteger(publicKeyHex, 16);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        BigInteger recoveredKey = Sign.signedMessageToKey(messageBytes, signature);
        boolean isVerified = recoveredKey.equals(publicKey);
        logger.debug("Signature verification result: {}", isVerified);
        return isVerified;
    }
}
