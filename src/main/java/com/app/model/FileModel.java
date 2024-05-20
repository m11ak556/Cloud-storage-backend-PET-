package com.app.model;

import com.app.model.id.FileModelId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Сущность модели файлов
 */
@Entity(name = "file")
@Table(
        uniqueConstraints=
            @UniqueConstraint(columnNames={"user_id", "name"})
)
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

    @Transient
    public String getTypeTitle() {
        return type.getTitle();
    }

}
