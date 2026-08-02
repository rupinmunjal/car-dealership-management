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
        return mapper.toPackageResponse(packageRepository.save(pkg));
    }

    @Transactional
    public PackageResponse update(Long id, PackageRequest request) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Package not found with id: " + id));
        mapper.updatePackageEntity(pkg, request);
        return mapper.toPackageResponse(packageRepository.save(pkg));
    }

    @Transactional
    public void delete(Long id) {
        if (!packageRepository.existsById(id)) {
            throw new EntityNotFoundException("Package not found with id: " + id);
        }
        packageRepository.deleteById(id);
    }
}
