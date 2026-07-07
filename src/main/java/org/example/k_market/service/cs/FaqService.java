package org.example.k_market.service.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Faq;
import org.example.k_market.repository.FaqRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
}