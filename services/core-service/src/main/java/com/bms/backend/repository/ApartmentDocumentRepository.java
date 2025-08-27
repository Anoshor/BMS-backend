package com.bms.backend.repository;

import com.bms.backend.entity.Apartment;
import com.bms.backend.entity.ApartmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApartmentDocumentRepository extends JpaRepository<ApartmentDocument, UUID> {
    
    List<ApartmentDocument> findByApartment(Apartment apartment);
    
    List<ApartmentDocument> findByApartmentAndIsActive(Apartment apartment, Boolean isActive);
    
    @Query("SELECT ad FROM ApartmentDocument ad WHERE ad.apartment.id = :apartmentId AND ad.isActive = true")
    List<ApartmentDocument> findActiveDocumentsByApartmentId(@Param("apartmentId") UUID apartmentId);
    
    List<ApartmentDocument> findByDocumentType(String documentType);
    
    @Query("SELECT ad FROM ApartmentDocument ad WHERE ad.apartment.id = :apartmentId AND ad.documentType = :documentType AND ad.isActive = true")
    List<ApartmentDocument> findByApartmentIdAndDocumentType(@Param("apartmentId") UUID apartmentId, 
                                                            @Param("documentType") String documentType);
    
    @Query("SELECT COUNT(ad) FROM ApartmentDocument ad WHERE ad.apartment.id = :apartmentId AND ad.isActive = true")
    Long countActiveDocumentsByApartmentId(@Param("apartmentId") UUID apartmentId);
}