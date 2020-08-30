package com.epicnerf.api;

import com.epicnerf.exception.AuthenticationException;
import com.epicnerf.hibernate.dao.ImageDataDao;
import com.epicnerf.hibernate.dao.UserDao;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.model.ImageData;
import com.epicnerf.hibernate.model.User;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.imageio.ImageIO;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Random;

@Service
public class ApiSupport {

    private final Random rand = new Random();
    @Autowired
    private UserDao userDao;
    @Autowired
    private ImageDataDao imageDataDao;
    @Value("${admin.token}")
    private String applicationAdminToken;

    private static HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        throw new NoResultException();
    }

    public User getCurrentUser() {
        try {
            HttpServletRequest request = getCurrentHttpRequest();
            String token = request.getHeader("X-API-KEY");
            if (token == null || token.isEmpty()) {
                throw new NoResultException();
            }
            return userDao.getUserWithToken(token);
        } catch (NoResultException e) {
            throw new AuthenticationException();
        }
    }

    public boolean hasAdminToken() {
        if (applicationAdminToken != null && applicationAdminToken.length() >= 100) {
            HttpServletRequest request = getCurrentHttpRequest();
            String token = request.getHeader("X-ADMIN-TOKEN");
            return applicationAdminToken.equals(token);
        }
        return false;
    }

    public void validateEmail(String email) {
        if (email != null) {
            if (EmailValidator.getInstance().isValid(email)) {
                return;
            }
        }
        throw new NoResultException();
    }

    public ImageData defaultUserImage() {
        return loadRandomImageDataFromResourcePath("/img/defaultUserImages");
    }

    public ImageData defaultGroupImage() {
        return loadRandomImageDataFromResourcePath("/img/defaultGroupImages");
    }

    private ImageData loadRandomImageDataFromResourcePath(String path) {
        String file = getRandomFilePathFromClassPathPath(path);
        try (InputStream ir = getClass().getResourceAsStream(file)) {
            BufferedImage bi = ImageIO.read(ir);
            return imageDataDao.getImageDataForFile(bi);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getRandomFilePathFromClassPathPath(String path) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(path + "/*");
            return path + "/" + resources[(rand.nextInt(resources.length))].getFilename();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String defaultGroupName() {
        String[] names = new String[]{
                "Happy Family", "Spoke Folks", "A Cuddle of Pandas", "Home Sweet Home", "All in the Family", "Kinfolk",
                "The Branches", "Blood Relatives", "Married With Kids", "The Cool Gang",
                "The Godfather and His Advisors", "Bromance", "My Folks", "Chatty Familia", "My Wife and Kids",
                "The Talent Gene Pool", "Family Bush", "Nuts and Bolts", "The Talkative Tribe", "Family Ties",
                "Sibling Signals", "We Are Family", "Folk and Kin", "Sister Sisters"};

        return names[rand.nextInt(names.length)];
    }

    public void validateUserIsInGroup(GroupObject group, Integer userId) {
        for (User user : group.getUsers()) {
            if (user.getId().equals(userId)) {
                return;
            }
        }
        throw new NoResultException();
    }
}
