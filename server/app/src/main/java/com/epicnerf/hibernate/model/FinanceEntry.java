package com.epicnerf.hibernate.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class FinanceEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modify_date", nullable = false)
    private Date modifyDate;

    @Column(nullable = false)
    private String title;

    @ManyToOne(optional = false)
    private User spentFrom;

    @ManyToOne
    private User createdBy;

    @ManyToOne(optional = false)
    private GroupObject group;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FinanceEntryEntry> entries;

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public GroupObject getGroup() {
        return group;
    }

    public void setGroup(GroupObject group) {
        this.group = group;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public List<FinanceEntryEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<FinanceEntryEntry> entries) {
        this.entries = entries;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getSpentFrom() {
        return spentFrom;
    }

    public void setSpentFrom(User spentFrom) {
        this.spentFrom = spentFrom;
    }

}