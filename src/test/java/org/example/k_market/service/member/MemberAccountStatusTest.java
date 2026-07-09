package org.example.k_market.service.member;

import org.example.k_market.dto.MemberDTO;
import org.example.k_market.dto.UsersDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberAccountStatusTest {

    @Test
    void mapsDatabaseValuesToLabels() {
        assertEquals("정상", MemberAccountStatus.ACTIVE.getLabel());
        assertEquals("중지", MemberAccountStatus.STOPPED.getLabel());
        assertEquals("휴면", MemberAccountStatus.INACTIVE.getLabel());
        assertEquals("탈퇴", MemberAccountStatus.WITHDRAWN.getLabel());
    }

    @Test
    void normalizesLegacyKoreanAndEnglishValues() {
        assertEquals(MemberAccountStatus.ACTIVE, MemberAccountStatus.from("정상"));
        assertEquals(MemberAccountStatus.STOPPED, MemberAccountStatus.from("SUSPENDED"));
        assertEquals(MemberAccountStatus.INACTIVE, MemberAccountStatus.from("휴면"));
        assertEquals(MemberAccountStatus.WITHDRAWN, MemberAccountStatus.from("탈퇴"));
    }

    @Test
    void permitsOnlyActiveAccountsToLogin() {
        assertFalse(MemberAccountStatus.isBlockedForLogin(MemberAccountStatus.ACTIVE));
        assertTrue(MemberAccountStatus.isBlockedForLogin(MemberAccountStatus.STOPPED));
        assertTrue(MemberAccountStatus.isBlockedForLogin(MemberAccountStatus.INACTIVE));
        assertTrue(MemberAccountStatus.isBlockedForLogin(MemberAccountStatus.WITHDRAWN));
        assertTrue(MemberAccountStatus.isBlockedForLogin(null));
    }

    @Test
    void dtoConversionStoresCanonicalDatabaseValues() {
        UsersDTO usersDTO = UsersDTO.builder().status("중지").build();
        MemberDTO memberDTO = MemberDTO.builder().status("휴면").build();

        assertEquals(MemberAccountStatus.STOPPED, usersDTO.toEntity().getStatus());
        assertEquals(MemberAccountStatus.INACTIVE, memberDTO.toEntity().getStatus());
        assertEquals("STOPPED", usersDTO.toEntity().toDto().getStatus());
        assertEquals("INACTIVE", memberDTO.toEntity().toDTO().getStatus());
    }
}
