package com.elevate.consultingplatform.entity.zoholeads;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "leads", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "ux_leads_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String zohoLeadId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String company;
    private Integer ainotseScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status = LeadStatus.PENDING;

    private String source;

    @Column(columnDefinition = "TEXT")
    private String ainotseSummary;

    private String inviteToken;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
