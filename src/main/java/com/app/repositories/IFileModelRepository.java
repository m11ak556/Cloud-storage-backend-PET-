package com.app.repositories;

import com.app.model.FileModel;
import com.app.model.id.FileModelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IFileModelRepository extends JpaRepository<FileModel, FileModelId> {
    Optional<FileModel> findByNameAndPath(String name, String path);
}
