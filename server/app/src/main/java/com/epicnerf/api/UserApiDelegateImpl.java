package com.epicnerf.api;

import com.epicnerf.hibernate.MapToOpenApiModel;
import com.epicnerf.hibernate.dao.GroupInviteDao;
import com.epicnerf.hibernate.dao.ImageDataDao;
import com.epicnerf.hibernate.dao.NotificationDao;
import com.epicnerf.hibernate.dao.UserDao;
import com.epicnerf.hibernate.model.GroupInvite;
import com.epicnerf.hibernate.model.ImageData;
import com.epicnerf.hibernate.repository.UserRepository;
import com.epicnerf.model.Invite;
import com.epicnerf.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserApiDelegateImpl implements UserApiDelegate {

    @Autowired
    private ImageDataDao imageDataDao;

    @Autowired
    private ApiSupport apiSupport;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupInviteDao groupInviteDao;

    @Autowired
    private MapToOpenApiModel openApiMapper;

    @Autowired
    private UserDao userDao;
    @Autowired
    private NotificationDao notificationDao;

    public ResponseEntity<User> userGet() {
        User u = openApiMapper.map(apiSupport.getCurrentUser());
        return ResponseEntity.ok(u);
    }

    public ResponseEntity<Void> userPut(User user) {
        Optional<com.epicnerf.hibernate.model.User> u = userRepository.findById(user.getId());
        if (u.isPresent() && userDao.isModifiable(u.get())) {
            u.get().setName(user.getName());
            if (user.getImage() != null && user.getImage().getUrl() != null) {
                ImageData imageData = imageDataDao.getImageDataFromUpload(user.getImage().getUrl());
                u.get().setImage(imageData);
            }
            userRepository.save(u.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new NoResultException();
    }

    public ResponseEntity<List<Invite>> userInviteGet() {
        com.epicnerf.hibernate.model.User user = apiSupport.getCurrentUser();
        List<GroupInvite> invites = groupInviteDao.getGroupInvites(user);
        return ResponseEntity.ok(openApiMapper.mapInvites(invites, user));
    }

    public ResponseEntity<Void> userResetApiKeyPut() {
        com.epicnerf.hibernate.model.User u = apiSupport.getCurrentUser();
        u.setToken(UUID.randomUUID().toString());
        userRepository.save(u);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
