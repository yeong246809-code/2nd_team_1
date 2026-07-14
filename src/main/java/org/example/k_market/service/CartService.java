package org.example.k_market.service;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.CartItemViewDTO;
import org.example.k_market.entity.Cart;
import org.example.k_market.entity.Product;
import org.example.k_market.entity.ProductSku;
import org.example.k_market.repository.CartRepository;
import org.example.k_market.repository.ProductRepository;
import org.example.k_market.repository.ProductSkuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;

    public List<CartItemViewDTO> getItems(int memberNo) {
        return cartRepository.findByMemberNoOrderByCreatedAtDesc(memberNo).stream()
                .map(this::toView)
                .toList();
    }

    public List<CartItemViewDTO> getSelectedItems(int memberNo, List<Long> cartNos) {
        if (cartNos == null || cartNos.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품을 선택해주세요.");
        }
        List<Cart> carts = cartRepository.findByMemberNoAndCartNoIn(memberNo, cartNos.stream().distinct().toList());
        if (carts.size() != cartNos.stream().distinct().count()) {
            throw new IllegalArgumentException("본인의 장바구니 상품만 주문할 수 있습니다.");
        }
        List<CartItemViewDTO> items = carts.stream().map(this::toView).toList();
        if (items.stream().anyMatch(CartItemViewDTO::isSoldOut)) {
            throw new IllegalArgumentException("품절되었거나 재고가 부족한 상품은 주문할 수 없습니다.");
        }
        return items;
    }

    public CartItemViewDTO previewProduct(long prodNo, Long skuNo, int quantity) {
        Product product = requireProduct(prodNo);
        ProductSku sku = requireSku(product, skuNo);
        validateQuantity(quantity, availableStock(product, sku));
        return buildView(0, product, sku, quantity);
    }

    @Transactional
    public void add(int memberNo, long prodNo, Long skuNo, int quantity) {
        Product product = requireProduct(prodNo);
        ProductSku sku = requireSku(product, skuNo);
        int stock = availableStock(product, sku);
        validateQuantity(quantity, stock);
        Cart existing = cartRepository.findMatchingItem(memberNo, prodNo, skuNo).orElse(null);
        int mergedQuantity = quantity + (existing == null ? 0 : existing.getQuantity());
        validateQuantity(mergedQuantity, stock);

        if (existing != null) {
            existing.changeQuantity(mergedQuantity);
            return;
        }
        cartRepository.save(Cart.builder()
                .memberNo(memberNo)
                .prodNo(prodNo)
                .skuNo(skuNo)
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void updateQuantity(int memberNo, long cartNo, int quantity) {
        Cart cart = requireOwnedCart(memberNo, cartNo);
        Product product = requireProduct(cart.getProdNo());
        ProductSku sku = requireSku(product, cart.getSkuNo());
        validateQuantity(quantity, availableStock(product, sku));
        cart.changeQuantity(quantity);
    }

    @Transactional
    public void deleteSelected(int memberNo, List<Long> cartNos) {
        if (cartNos == null || cartNos.isEmpty()) {
            throw new IllegalArgumentException("삭제할 상품을 선택해주세요.");
        }
        List<Long> distinctNos = cartNos.stream().distinct().toList();
        List<Cart> carts = cartRepository.findByMemberNoAndCartNoIn(memberNo, distinctNos);
        if (carts.size() != distinctNos.size()) {
            throw new IllegalArgumentException("본인의 장바구니 상품만 삭제할 수 있습니다.");
        }
        cartRepository.deleteAll(carts);
    }

    @Transactional
    public int deleteSoldOut(int memberNo) {
        List<Cart> soldOut = cartRepository.findByMemberNoOrderByCreatedAtDesc(memberNo).stream()
                .filter(cart -> toView(cart).isSoldOut())
                .toList();
        cartRepository.deleteAll(soldOut);
        return soldOut.size();
    }

    private Cart requireOwnedCart(int memberNo, long cartNo) {
        return cartRepository.findByCartNoAndMemberNo(cartNo, memberNo)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 상품을 찾을 수 없습니다."));
    }

    private Product requireProduct(long prodNo) {
        return productRepository.findById(prodNo)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다: " + prodNo));
    }

    private ProductSku requireSku(Product product, Long skuNo) {
        List<ProductSku> skus = productSkuRepository.findByProdNoOrderBySkuNoAsc(product.getProdNo());
        if (skuNo == null) {
            if (!skus.isEmpty()) {
                throw new IllegalArgumentException("상품 옵션을 선택해주세요.");
            }
            return null;
        }
        return productSkuRepository.findBySkuNoAndProdNo(skuNo, product.getProdNo())
                .orElseThrow(() -> new IllegalArgumentException("선택한 상품 옵션이 올바르지 않습니다."));
    }

    private int availableStock(Product product, ProductSku sku) {
        return sku == null
                ? Math.max(product.getStockQuantity() == null ? 0 : product.getStockQuantity(), 0)
                : Math.max(sku.getStock() == null ? 0 : sku.getStock(), 0);
    }

    private void validateQuantity(int quantity, int stock) {
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        if (stock < quantity) {
            throw new IllegalArgumentException(stock <= 0 ? "품절된 상품입니다." : "재고는 최대 " + stock + "개입니다.");
        }
    }

    private CartItemViewDTO toView(Cart cart) {
        Product product = requireProduct(cart.getProdNo());
        ProductSku sku = cart.getSkuNo() == null ? null
                : productSkuRepository.findBySkuNoAndProdNo(cart.getSkuNo(), product.getProdNo()).orElse(null);
        return buildView(cart.getCartNo(), product, sku, cart.getQuantity());
    }

    private CartItemViewDTO buildView(long cartNo, Product product, ProductSku sku, int quantity) {
        int basePrice = product.getPrice() == null ? 0 : product.getPrice();
        int optionPrice = sku == null || sku.getPrice() == null ? 0 : sku.getPrice();
        int price = basePrice + optionPrice;
        int discountRate = product.getDiscountRate() == null ? 0 : product.getDiscountRate();
        int discountedPrice = (int) (price * (100L - discountRate) / 100L);
        int shippingFee = product.getShippingFee() == null ? 0 : product.getShippingFee();
        int rewardPoints = product.getRewardPoints() == null ? 0 : product.getRewardPoints();
        int stock = availableStock(product, sku);

        return CartItemViewDTO.builder()
                .cartNo(cartNo)
                .prodNo(product.getProdNo())
                .skuNo(sku == null ? null : sku.getSkuNo())
                .skuName(sku == null ? null : sku.getSkuName())
                .name(product.getName())
                .description(product.getDescription())
                .thumb1(product.getThumb1())
                .quantity(quantity)
                .unitPrice(price)
                .discountRate(discountRate)
                .rewardPoints(rewardPoints)
                .shippingFee(shippingFee)
                .lineTotal(discountedPrice * quantity + shippingFee)
                .lineRewardPoints(rewardPoints * quantity)
                .maxQuantity(stock)
                .soldOut(stock < quantity || stock <= 0)
                .build();
    }
}
