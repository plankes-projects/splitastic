package com.epicnerf.api;

import com.epicnerf.hibernate.MapToHibernateModel;
import com.epicnerf.hibernate.MapToOpenApiModel;
import com.epicnerf.hibernate.model.FinanceEntry;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.FinanceEntryRepository;
import com.epicnerf.model.FinanceEntryEntry;
import com.epicnerf.service.NotificationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.Optional;

@Service
public class FinanceApiDelegateImpl implements FinanceApiDelegate {

    @Autowired
    private ApiSupport apiSupport;

    @Autowired
    private FinanceEntryRepository financeEntryRepository;

    @Autowired
    private MapToHibernateModel mapper;

    @Autowired
    private MapToOpenApiModel openApiMapper;

    @Autowired
    private NotificationManager notificationManager;

    public ResponseEntity<Void> financeFinanceIdDelete(Integer financeId) {
        Optional<FinanceEntry> finance = financeEntryRepository.findById(financeId);

        User user = apiSupport.getCurrentUser();
        if (finance.isPresent() && canDelete(finance.get(), user)) {
            financeEntryRepository.delete(finance.get());
            notificationManager.onFinanceEntryDelete(user, finance.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new NoResultException();
    }

    private boolean canDelete(FinanceEntry financeEntry, User user) {
        if (financeEntry.getSpentFrom().getId().equals(user.getId())) {
            return true;
        }

        if (financeEntry.getGroup().getOwner().getId().equals(user.getId()) && financeEntry.getSpentFrom().isVirtualUser()) {
            return true;
        }

        return false;
    }

    public ResponseEntity<com.epicnerf.model.FinanceEntry> financeFinanceIdGet(Integer financeId) {
        User user = apiSupport.getCurrentUser();
        Optional<FinanceEntry> finance = financeEntryRepository.findById(financeId);
        if (finance.isPresent()) {
            apiSupport.validateUserIsInGroup(finance.get().getGroup(), user.getId());
            com.epicnerf.model.FinanceEntry entry = openApiMapper.map(finance.get());
            return ResponseEntity.ok(entry);
        }

        throw new NoResultException();
    }

    public ResponseEntity<Void> financePut(com.epicnerf.model.FinanceEntry financeEntry) {
        User user = apiSupport.getCurrentUser();
        Optional<FinanceEntry> finance = financeEntryRepository.findById(financeEntry.getId());
        if (finance.isPresent() && finance.get().getSpentFrom().getId().equals(user.getId())) {
            FinanceEntry f = mapper.mapFinance(financeEntry, true);
            f.setGroup(finance.get().getGroup());
            apiSupport.validateUserIsInGroup(finance.get().getGroup(), financeEntry.getSpentFrom());
            for (FinanceEntryEntry entry : financeEntry.getSpent()) {
                apiSupport.validateUserIsInGroup(finance.get().getGroup(), entry.getSpentFor());
            }

            financeEntryRepository.save(f);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        throw new NoResultException();
    }
}
