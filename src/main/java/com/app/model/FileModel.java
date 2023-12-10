package com.app.model;

import com.app.model.id.FileModelId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Entity(name = "file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(FileModelId.class)
public class FileModel {

    @Id
    @GeneratedValue
    private long id;
    @Id
    @Column(name = "user_id")
    private long userId;

    @Column(unique = true)
    private String name;
    private String path;
    private Date dateCreated;

    @Enumerated(EnumType.STRING)
    private FileTypes type;
    private long size;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

}
