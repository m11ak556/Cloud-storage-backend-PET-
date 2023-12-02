package com.app.repositories;

import com.app.model.FileModel;
import com.app.model.id.FileModelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFileModelRepository extends JpaRepository<FileModel, FileModelId> {
}
