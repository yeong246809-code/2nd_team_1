package org.example.k_market.dto;

import org.example.k_market.entity.SiteConfig;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SiteConfigDTO {

    private int id;
    private String title;
    private String subtitle;
    private String headerLogo_origin;
    private String headerLogo_stored;
    private String footerLogo_origin;
    private String footerLogo_stored;
    private String favicon_origin;
    private String favicon_stored;
    private String companyName;
    private String ceo;
    private String bizNumber;
    private String mailOrderNumber;
    private String baseAddress;
    private String detailAddress;
    private String csPhone;
    private String csHours;
    private String csEmail;
    private String finDisputeManager;
    private String copyright;

    public SiteConfig toEntity(){
        return SiteConfig.builder()
                .id(id).title(title).subtitle(subtitle)
                .headerLogo_origin(headerLogo_origin).headerLogo_stored(headerLogo_stored)
                .footerLogo_origin(footerLogo_origin).footerLogo_stored(footerLogo_stored)
                .favicon_origin(favicon_origin).favicon_stored(favicon_stored)
                .companyName(companyName).ceo(ceo).bizNumber(bizNumber)
                .mailOrderNumber(mailOrderNumber).baseAddress(baseAddress).detailAddress(detailAddress)
                .csPhone(csPhone).csHours(csHours).csEmail(csEmail).finDisputeManager(finDisputeManager)
                .copyright(copyright).build();
    }
}