package com.example.springblog.security;

import com.example.springblog.exception.SpringBlogException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Service
public class JwtProvider {

    private KeyStore keyStore;

    @PostConstruct
    public void init(){

        try {
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourseAsStream = getClass().getResourceAsStream("/springblog.jks");
            keyStore.load(resourseAsStream, "Saurabh@123".toCharArray());
        }catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e){
            throw new SpringBlogException("exception occurred during loading from keystore");
        }
    }

    public String generateToken(Authentication authentication){
        User principal = (User) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .signWith(getPrivatekey())
                .compact();
    }

    private PrivateKey getPrivatekey() {
        try {
            return (PrivateKey)keyStore.getKey("springblog","Saurabh@123".toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new SpringBlogException("Exception occurred while retrieving private key");
        }

    }

    public boolean validateToken(String jwt){
        Jwts.parser().setSigningKey(getPublicKey()).parseClaimsJws(jwt);
        return true;
    }

    private PublicKey getPublicKey() {
        try {
            return keyStore.getCertificate("Springblog").getPublicKey();
        }catch (KeyStoreException e){
            throw new SpringBlogException("exception occurred while retrieving public key");
        }
    }

    public String  getUserNameFromJWT(String token) {

        Claims claims = Jwts.parser()
                            .setSigningKey(getPublicKey())
                            .parseClaimsJws(token)
                            .getBody();
        return claims.getSubject();

    }
}
