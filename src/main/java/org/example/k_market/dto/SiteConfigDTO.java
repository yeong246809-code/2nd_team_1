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
    private String headerLogo;
    private String footerLogo;
    private String favicon;
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
                .id(id).title(title).subtitle(subtitle).headerLogo(headerLogo)
                .footerLogo(footerLogo).favicon(favicon).companyName(companyName)
                .ceo(ceo).bizNumber(bizNumber).mailOrderNumber(mailOrderNumber)
                .baseAddress(baseAddress).detailAddress(detailAddress)
                .csPhone(csPhone).csHours(csHours).csEmail(csEmail)
                .finDisputeManager(finDisputeManager).copyright(copyright)
                .build();
    }
}