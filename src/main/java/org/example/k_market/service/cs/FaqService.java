package org.example.k_market.service.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.FaqDTO;
import org.example.k_market.entity.Faq;
import org.example.k_market.repository.FaqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    public Map<String, List<Faq>> findFaqGroupsByType1(String type1) {

        List<Faq> faqs = faqRepository.findByType1OrderByNoAsc(type1);

        return faqs.stream()
                .collect(Collectors.groupingBy(
                        Faq::getType2,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    public List<Faq> findAll() {
        return faqRepository.findAllByOrderByNoDesc();
    }

    public List<Faq> findByType(String type) {
        if (type == null || type.isBlank() || type.equals("전체")) {
            return findAll();
        }

        return faqRepository.findByType1OrderByNoAsc(type);
    }

    public Faq findById(int no) {
        return faqRepository.findById(no)
                .orElseThrow(() -> new IllegalArgumentException("FAQ가 없습니다. no=" + no));
    }

    @Transactional
    public Faq save(FaqDTO dto) {
        return faqRepository.save(dto.toEntity());
    }

    @Transactional
    public Faq update(int no, FaqDTO dto) {
        Faq faq = findById(no);

        faq.setType1(dto.getType1());
        faq.setType2(dto.getType2());
        faq.setTitle(dto.getTitle());
        faq.setContent(dto.getContent());

        return faq;
    }

    @Transactional
    public void delete(int no) {
        faqRepository.deleteById(no);
    }

    @Transactional
    public void deleteChecked(List<Integer> nos) {
        faqRepository.deleteAllById(nos);
    }
}