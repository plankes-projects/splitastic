package com.epicnerf.api;

import com.epicnerf.hibernate.MapToOpenApiModel;
import com.epicnerf.hibernate.dao.ChoreDao;
import com.epicnerf.hibernate.dao.GroupObjectDao;
import com.epicnerf.hibernate.model.Chore;
import com.epicnerf.hibernate.model.ChoreEntry;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.ChoreEntryRepository;
import com.epicnerf.hibernate.repository.ChoreRepository;
import com.epicnerf.hibernate.repository.GroupObjectRepository;
import com.epicnerf.model.ChoreSummary;
import com.epicnerf.service.ExportService;
import com.epicnerf.service.NotificationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

@Service
public class ChoreApiDelegateImpl implements ChoreApiDelegate {

    @Autowired
    private ApiSupport apiSupport;

    @Autowired
    private ChoreRepository choreRepository;
    @Autowired
    private ChoreEntryRepository choreEntryRepository;
    @Autowired
    private GroupObjectRepository groupObjectRepository;
    @Autowired
    private ChoreDao choreDao;

    @Autowired
    private MapToOpenApiModel mapToOpenApiModel;

    @Autowired
    private NotificationManager notificationManager;

    @Autowired
    private GroupObjectDao groupObjectDao;
    @Autowired
    private ExportService exportService;

    public ResponseEntity<Void> choreChoreIdDelete(Integer choreId) {
        User user = apiSupport.getCurrentUser();
        Optional<Chore> chore = choreRepository.findById(choreId);

        if (chore.isPresent() && chore.get().getGroup().getOwner().getId().equals(user.getId())) {
            choreDao.deleteChore(chore.get());
            notificationManager.onChoreDeleted(user, chore.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new NoResultException();
    }

    public ResponseEntity<Void> choreChoreIdEntryDelete(Integer choreId) {
        User user = apiSupport.getCurrentUser();
        Optional<Chore> chore = choreRepository.findById(choreId);
        if (chore.isPresent()) {
            apiSupport.validateUserIsInGroup(chore.get().getGroup(), user.getId());
            choreDao.deleteLatestChoreEntry(chore.get(), user);
            notificationManager.onChoreEntryDeleted(user, chore.get());
            groupObjectDao.updateActivity(chore.get().getGroup());
            return new ResponseEntity<>(HttpStatus.OK);
        }

        throw new NoResultException();
    }

    public ResponseEntity<Void> choreChoreIdPost(Integer choreId) {
        User user = apiSupport.getCurrentUser();
        Optional<Chore> chore = choreRepository.findById(choreId);
        if (chore.isPresent()) {
            apiSupport.validateUserIsInGroup(chore.get().getGroup(), user.getId());

            ChoreEntry entry = new ChoreEntry();
            entry.setChore(chore.get());
            entry.setUser(user);
            choreEntryRepository.save(entry);
            notificationManager.onChoreEntryAdded(user, chore.get());
            groupObjectDao.updateActivity(chore.get().getGroup());
            return new ResponseEntity<>(HttpStatus.OK);
        }

        throw new NoResultException();
    }

    public ResponseEntity<List<ChoreSummary>> choreSummariesGroupIdGet(Integer groupId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> group = groupObjectRepository.findById(groupId);
        if (group.isPresent()) {
            apiSupport.validateUserIsInGroup(group.get(), user.getId());
            return ResponseEntity.ok(choreDao.getAllChoreSummaries(group.get(), group.get().getOwner().getId().equals(user.getId())));
        }

        throw new NoResultException();
    }

    public ResponseEntity<Integer> chorePost(Integer groupId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> group = groupObjectRepository.findById(groupId);

        if (group.isPresent() && group.get().getOwner().getId().equals(user.getId())) {
            Chore chore = new Chore();
            chore.setTitle("New Chore");
            chore.setDescription("Description");
            chore.setGroup(group.get());

            choreRepository.save(chore);
            notificationManager.onChoreAdded(user, chore);
            return ResponseEntity.ok(chore.getId());
        }
        throw new NoResultException();
    }

    public ResponseEntity<Void> chorePut(com.epicnerf.model.Chore chore) {
        User user = apiSupport.getCurrentUser();
        Optional<Chore> c = choreRepository.findById(chore.getId());

        if (c.isPresent() && c.get().getGroup().getOwner().getId().equals(user.getId())) {
            c.get().setTitle(chore.getTitle());
            c.get().setDescription(chore.getDescription());
            choreRepository.save(c.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new NoResultException();
    }

    public ResponseEntity<com.epicnerf.model.Chore> choreChoreIdGet(Integer choreId) {
        User user = apiSupport.getCurrentUser();
        Optional<Chore> c = choreRepository.findById(choreId);

        if (c.isPresent()) {
            apiSupport.validateUserIsInGroup(c.get().getGroup(), user.getId());
            return ResponseEntity.ok(mapToOpenApiModel.map(c.get(), c.get().getGroup().getOwner().getId().equals(user.getId())));
        }
        throw new NoResultException();
    }

    public ResponseEntity<org.springframework.core.io.Resource> choreExportGroupGroupIdGet(Integer groupId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> group = groupObjectRepository.findById(groupId);
        if (group.isPresent()) {
            apiSupport.validateUserIsInGroup(group.get(), user.getId());
            String csvString = exportService.getChoreExportDataByGroupIdAsCsvString(groupId);
            Resource resource = new ByteArrayResource(csvString.getBytes());
            return ResponseEntity.ok(resource);
        }

        throw new NoResultException();
    }
}
