package org.example.k_market.service.member;

import org.example.k_market.dto.MyInfoUpdateDTO;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Shop;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyInfoServiceTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ShopRepository shopRepository;

    private MyInfoService myInfoService;

    @BeforeEach
    void setUp() {
        myInfoService = new MyInfoService(usersRepository, memberRepository, shopRepository);
    }

    @Test
    void updatesMemberSignupInformation() {
        Users user = Users.builder()
                .memberNo(1)
                .id("user01")
                .role("USER")
                .build();
        Member member = Member.builder()
                .memberNo(1)
                .name("기존이름")
                .birthDate(LocalDate.of(2000, 1, 1))
                .email("old@example.com")
                .phone("01011112222")
                .status(MemberAccountStatus.ACTIVE)
                .build();
        MyInfoUpdateDTO dto = new MyInfoUpdateDTO();
        dto.setName("새이름");
        dto.setEmailLocal("new");
        dto.setEmailDomain("example.com");
        dto.setPhoneFirst("010");
        dto.setPhoneMiddle("3333");
        dto.setPhoneLast("4444");
        dto.setZipCode("12345");
        dto.setBaseAddress("서울시");
        dto.setDetailAddress("101호");

        when(usersRepository.findById("user01")).thenReturn(Optional.of(user));
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));

        myInfoService.update("user01", dto);

        verify(memberRepository).save(org.mockito.ArgumentMatchers.argThat(updated ->
                "새이름".equals(updated.getName())
                        && "new@example.com".equals(updated.getEmail())
                        && "01033334444".equals(updated.getPhone())
                        && LocalDate.of(2000, 1, 1).equals(updated.getBirthDate())));
    }

    @Test
    void updatesSellerProfileWithoutChangingBusinessNumber() {
        Users seller = Users.builder()
                .memberNo(2)
                .id("seller01")
                .role("SELLER")
                .build();
        Shop shop = Shop.builder()
                .memberNo(2)
                .name("기존상점")
                .ceo("기존대표")
                .bizNumber("123-45-67890")
                .mailOrderNumber("기존신고번호")
                .build();
        MyInfoUpdateDTO dto = new MyInfoUpdateDTO();
        dto.setShopName("새상점");
        dto.setCeo("새대표");
        dto.setMailOrderNumber("새신고번호");
        dto.setShopPhone("02-1234-5678");
        dto.setFax("02-1111-2222");
        dto.setShopZipCode("54321");
        dto.setShopBaseAddress("부산시");
        dto.setShopDetailAddress("202호");

        when(usersRepository.findById("seller01")).thenReturn(Optional.of(seller));
        when(shopRepository.findById(2)).thenReturn(Optional.of(shop));

        myInfoService.update("seller01", dto);

        assertThat(shop.getName()).isEqualTo("새상점");
        assertThat(shop.getCeo()).isEqualTo("새대표");
        assertThat(shop.getMailOrderNumber()).isEqualTo("새신고번호");
        assertThat(shop.getBizNumber()).isEqualTo("123-45-67890");
    }
}
