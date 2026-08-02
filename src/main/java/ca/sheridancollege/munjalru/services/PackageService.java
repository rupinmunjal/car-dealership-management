package ca.sheridancollege.munjalru.services;

import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.dto.PackageRequest;
import ca.sheridancollege.munjalru.dto.PackageResponse;
import ca.sheridancollege.munjalru.mapper.CarDealerMapper;
import ca.sheridancollege.munjalru.repositories.PackageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class PackageService {

    private final PackageRepository packageRepository;
    private final CarDealerMapper mapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<PackageResponse> findAll(Pageable pageable) {
        return packageRepository.findAll(pageable).map(mapper::toPackageResponse);
    }

    @Transactional(readOnly = true)
    public PackageResponse findById(Long id) {
        return packageRepository.findById(id)
                .map(mapper::toPackageResponse)
                .orElseThrow(() -> new EntityNotFoundException("Package not found with id: " + id));
    }

    @Transactional
    public PackageResponse create(PackageRequest request) {
        Package pkg = mapper.toPackageEntity(request);
        Package saved = packageRepository.save(pkg);
        PackageResponse response = mapper.toPackageResponse(saved);
        auditLogService.record("PACKAGE_CREATED", "Package", saved.getId(), null, response);
        return response;
    }

    @Transactional
    public PackageResponse update(Long id, PackageRequest request) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Package not found with id: " + id));
        mapper.updatePackageEntity(pkg, request);
        PackageResponse response = mapper.toPackageResponse(packageRepository.save(pkg));
        auditLogService.record("PACKAGE_UPDATED", "Package", id, null, response);
        return response;
    }

    @Transactional
    public void delete(Long id) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Package not found with id: " + id));
        PackageResponse details = mapper.toPackageResponse(pkg);
        packageRepository.deleteById(id);
        auditLogService.record("PACKAGE_DELETED", "Package", id, null, details);
    }
}
