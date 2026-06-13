package com.hung.microoauth2auth.filter;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.KeyPair;

/**
 * @author Hung
 * @date 2022/7/15 22:07
 */
@Component
public class PasswordDecodeFilter
//        implements Filter
{
    @Resource
    private KeyPair keyPair;

//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        String password = servletRequest.getParameter("password");
//        if (password == null) {
//            throw new RuntimeException("密码为空");
//        }
//        byte[] decodeHex = HexUtil.decodeHex(password);
//        byte[] decrypt = new RSA(keyPair.getPrivate(), keyPair.getPublic()).decrypt(decodeHex, KeyType.PrivateKey);
//
//        servletRequest.setAttribute("password", new String(decrypt));
//        filterChain.doFilter(servletRequest, servletResponse);
//    }
}
