package com.hung.microoauth2auth.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/**
 * 获取RSA公钥接口
 *
 * @author Hung
 * @date 2021/11/2 23:14
 */
@RestController
public class KeyPairController {

    @Autowired
    private KeyPair keyPair;

//    @SneakyThrows
//    @GetMapping("/rsa/publicKey")
//    public String getKey() {
//        PublicKey publicKey = keyPair.getPublic();
//        StringWriter writer = new StringWriter();
//        PemWriter pemWriter = new PemWriter(writer);
//        pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
//        pemWriter.flush();
//        pemWriter.close();
//        return writer.toString();
//    }

    @GetMapping("/rsa/publicKey")
    public Map<String, Object> getKey() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAKey key = new RSAKey.Builder(publicKey).build();
        return new JWKSet(key).toJSONObject();
    }
}
