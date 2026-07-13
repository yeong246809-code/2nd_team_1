package org.example.k_market.service;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.ProductOptionItems;
import org.example.k_market.entity.ProductOptions;
import org.example.k_market.repository.ProductOptionItemsRepository;
import org.example.k_market.repository.ProductOptionsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOptionService {

    private final ProductOptionsRepository productOptionsRepository;
    private final ProductOptionItemsRepository productOptionItemsRepository;

    /**
     * 상품번호로 옵션 그룹 목록 조회
     */
    public List<ProductOptions> findOptionsByProdNo(Long prodNo) {

        if (prodNo == null) {
            return List.of();
        }

        return productOptionsRepository
                .findByProdNoOrderByOptionNoAsc(prodNo);
    }

    /**
     * 옵션 그룹별 옵션 항목 조회
     *
     * key   : optionNo
     * value : 해당 옵션에 속한 항목 목록
     */
    public Map<Long, List<ProductOptionItems>> findOptionItemsMap(
            List<ProductOptions> options) {

        Map<Long, List<ProductOptionItems>> optionItemsMap =
                new LinkedHashMap<>();

        if (options == null || options.isEmpty()) {
            return optionItemsMap;
        }

        for (ProductOptions option : options) {

            List<ProductOptionItems> items =
                    productOptionItemsRepository
                            .findByOptionNoOrderByOptItemNoAsc(
                                    option.getOptionNo()
                            );

            optionItemsMap.put(option.getOptionNo(), items);
        }

        return optionItemsMap;
    }

    /**
     * 상품 옵션 저장
     *
     * optionNames  예: ["색상", "사이즈"]
     * optionValues 예: ["블랙, 화이트", "S, M, L, XL"]
     */
    @Transactional
    public void saveOptions(
            Long prodNo,
            List<String> optionNames,
            List<String> optionValues) {

        if (prodNo == null) {
            throw new IllegalArgumentException(
                    "옵션을 저장할 상품 번호가 없습니다."
            );
        }

        if (optionNames == null || optionNames.isEmpty()) {
            return;
        }

        for (int i = 0; i < optionNames.size(); i++) {

            String optionName = optionNames.get(i);

            if (!StringUtils.hasText(optionName)) {
                continue;
            }

            String optionValue = "";

            if (optionValues != null && i < optionValues.size()) {
                optionValue = optionValues.get(i);
            }

            ProductOptions savedOption =
                    productOptionsRepository.save(
                            ProductOptions.builder()
                                    .prodNo(prodNo)
                                    .name(optionName.trim())
                                    .build()
                    );

            saveOptionItems(
                    savedOption.getOptionNo(),
                    optionValue
            );
        }
    }

    /**
     * 수정 시 기존 옵션 전체 삭제 후 새 옵션으로 교체
     */
    @Transactional
    public void replaceOptions(
            Long prodNo,
            List<String> optionNames,
            List<String> optionValues) {

        deleteOptionsByProdNo(prodNo);

        saveOptions(
                prodNo,
                optionNames,
                optionValues
        );
    }

    /**
     * 상품번호에 연결된 옵션과 옵션 항목 전체 삭제
     */
    @Transactional
    public void deleteOptionsByProdNo(Long prodNo) {

        if (prodNo == null) {
            return;
        }

        List<ProductOptions> options =
                productOptionsRepository
                        .findByProdNoOrderByOptionNoAsc(prodNo);

        for (ProductOptions option : options) {

            productOptionItemsRepository
                    .deleteByOptionNo(option.getOptionNo());
        }

        productOptionsRepository.deleteByProdNo(prodNo);
    }

    /**
     * 쉼표로 입력된 옵션항목을 나누어 저장
     *
     * 예: "S, M, L, XL"
     */
    private void saveOptionItems(
            long optionNo,
            String optionValue) {

        if (!StringUtils.hasText(optionValue)) {
            return;
        }

        String[] itemNames = optionValue.split(",");

        for (String itemName : itemNames) {

            if (!StringUtils.hasText(itemName)) {
                continue;
            }

            ProductOptionItems item =
                    ProductOptionItems.builder()
                            .optionNo(optionNo)
                            .itemName(itemName.trim())
                            .addPrice(0)
                            .stock(0)
                            .build();

            productOptionItemsRepository.save(item);
        }
    }
}