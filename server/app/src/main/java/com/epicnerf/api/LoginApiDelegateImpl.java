package com.epicnerf.api;

import com.epicnerf.hibernate.dao.LoginTokenDao;
import com.epicnerf.hibernate.dao.UserDao;
import com.epicnerf.hibernate.model.LoginToken;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.LoginTokenRepository;
import com.epicnerf.model.LoginData;
import com.epicnerf.model.LoginDataSuccess;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import javax.persistence.NoResultException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoginApiDelegateImpl implements LoginApiDelegate {

    Logger logger = LoggerFactory.getLogger(LoginApiDelegateImpl.class);

    @Autowired
    private ApiSupport apiSupport;


    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserDao userDao;

    @Autowired
    private LoginTokenRepository loginTokenRepository;

    @Autowired
    private LoginTokenDao loginTokenDao;

    @Value("${email.from}")
    private String emailFrom;

    @Value("${server.baseurl}")
    private String baseUrl;

    public static String generateRandomString(int length) {
        // You can customize the characters that you want to add into
        // the random strings
        String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String NUMBER = "0123456789";

        String DATA_FOR_RANDOM_STRING = CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();

        if (length < 1) throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            // 0-62 (exclusive), random returns 0-61
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            sb.append(rndChar);
        }

        return sb.toString();
    }

    public ResponseEntity<String> loginGet(String secret) {
        LoginToken token = loginTokenDao.getLoginTokenWithProof(secret);
        token.setApproved(true);
        loginTokenRepository.save(token);

        logger.info("Login token approved for: " + token.getUser().getEmail());

        return ResponseEntity.ok(loadEmailLinkClickedHtml());
    }

    public ResponseEntity<LoginData> loginPost(String email) {
        LoginData ret = new LoginData();
        ret.setEmail(email);
        ret.setToken(UUID.randomUUID().toString());
        ret.setVerify(generateRandomString(5).toUpperCase());

        User user = userDao.getOrCreateUserWithEmail(email);

        loginTokenDao.deleteLoginToken(user);

        boolean approved = apiSupport.hasAdminToken();

        LoginToken token = new LoginToken();
        token.setUser(user);
        token.setToken(ret.getToken());
        token.setApproved(approved);
        loginTokenRepository.save(token);

        if (!approved) {
            sendEmail(email, ret.getVerify(), token.getSecret());
        }
        return ResponseEntity.ok(ret);
    }

    private String loadEmailLinkClickedHtml() {
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/templates/loginEmailLinkClicked.html")))) {
                return br.lines().collect(Collectors.joining());
            }
        } catch (IOException e) {
            return "Your device is now loggedin! But something went wrong on the server that is why you get this ugly page.";
        }
    }

    private String loadEmailHtml(String verify, String secret) throws IOException {
        String url = baseUrl + "/login?secret=" + secret;
        String href = "<a href=\"" + url + "\">" + StringEscapeUtils.escapeHtml4(url) + "</a>";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/templates/loginEmail.html")))) {
            String template = br.lines().collect(Collectors.joining());
            template = template
                    .replace("!!CODE!!", verify)
                    .replace("!!url!!", href);
            return template;
        }
    }

    private void sendEmail(String email, String verify, String secret) {
        try {
            String html = loadEmailHtml(verify, secret);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(html, true);
            helper.setTo(email);
            helper.setSubject("Splitastic Login");
            helper.setFrom(emailFrom);
            javaMailSender.send(mimeMessage);
            logger.info("Sent login email to: " + email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<LoginDataSuccess> loginPut(String token) {
        LoginToken loginToken = loginTokenDao.getLoginTokenWithToken(token);
        if (!loginToken.getApproved()) {
            throw new NoResultException();
        }

        LoginDataSuccess ret = new LoginDataSuccess();
        ret.setToken(loginToken.getUser().getToken());
        ret.setUserId(loginToken.getUser().getId());

        //loginTokenRepository.delete(loginToken);
        return ResponseEntity.ok(ret);
    }
}
