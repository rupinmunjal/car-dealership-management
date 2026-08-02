package ca.sheridancollege.munjalru.services;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.dto.DealerRequest;
import ca.sheridancollege.munjalru.dto.DealerResponse;
import ca.sheridancollege.munjalru.mapper.CarDealerMapper;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepository dealerRepository;
    private final CarDealerMapper mapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<DealerResponse> findAll(Pageable pageable) {
        return dealerRepository.findAll(pageable).map(mapper::toDealerResponse);
    }

    @Transactional(readOnly = true)
    public DealerResponse findById(Long id) {
        return dealerRepository.findById(id)
                .map(mapper::toDealerResponse)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + id));
    }

    /**
     * Returns a single dealer wrapped as a {@link Page} for dealer-scoped
     * list endpoints. If {@code dealerId} is null returns an empty page.
     */
    @Transactional(readOnly = true)
    public Page<DealerResponse> findByIdAsPage(Long dealerId, Pageable pageable) {
        if (dealerId == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + dealerId));
        return new PageImpl<>(
                Collections.singletonList(mapper.toDealerResponse(dealer)),
                pageable, 1);
    }

    @Transactional
    public DealerResponse create(DealerRequest request) {
        Dealer dealer = mapper.toDealerEntity(request);
        if (dealer.getCars() == null) {
            dealer.setCars(new ArrayList<>());
        }
        Dealer saved = dealerRepository.save(dealer);
        DealerResponse response = mapper.toDealerResponse(saved);
        auditLogService.record("DEALER_CREATED", "Dealer", saved.getId(), saved.getId(), response);
        return response;
    }

    @Transactional
    public DealerResponse update(Long id, DealerRequest request) {
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + id));

        mapper.updateDealerEntity(dealer, request);
        DealerResponse response = mapper.toDealerResponse(dealerRepository.save(dealer));
        auditLogService.record("DEALER_UPDATED", "Dealer", id, id, response);
        return response;
    }

    @Transactional
    public void delete(Long id) {
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + id));
        DealerResponse details = mapper.toDealerResponse(dealer);
        dealerRepository.deleteById(id);
        auditLogService.record("DEALER_DELETED", "Dealer", id, id, details);
    }

    @Transactional(readOnly = true)
    public long countTotalInventory() {
        return dealerRepository.findAll().stream()
                .mapToLong(d -> d.getCars() == null ? 0 : d.getCars().size())
                .sum();
    }
}
