package org.example.k_market.dto;

import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@ToString
public class PageResponseDTO<T> {

    private List<T> dtoList; // 실제 데이터 목록
    private int total;       // 전체 데이터 수
    private int pg;          // 현재 페이지 번호 (1부터 시작)
    private int size;        // 페이지당 데이터 수 (5개)

    // 페이지 블록 계산용
    private int startPage;   // 현재 블록의 시작 페이지
    private int endPage;     // 현재 블록의 끝 페이지
    private boolean prev;    // '이전' 버튼 활성화 여부
    private boolean next;    // '다음' 버튼 활성화 여부
    private int prevPage;    // '이전' 버튼 클릭 시 이동할 페이지
    private int nextPage;    // '다음' 버튼 클릭 시 이동할 페이지

    public PageResponseDTO(Page<T> pageResult, int blockSize) {
        this.dtoList = pageResult.getContent();
        this.total = (int) pageResult.getTotalElements();
        this.pg = pageResult.getNumber() + 1; // JPA는 0부터 시작하므로 +1
        this.size = pageResult.getSize();

        // 1. 끝 페이지 계산 (예: pg가 3이면 ceil(3/5)*5 = 5)
        this.endPage = (int) (Math.ceil(this.pg / (double) blockSize)) * blockSize;

        // 2. 시작 페이지 계산
        this.startPage = this.endPage - (blockSize - 1);

        // 3. 실제 마지막 페이지 계산
        int lastPage = (int) (Math.ceil(total / (double) size));
        if (lastPage == 0) lastPage = 1; // 데이터가 하나도 없을 때 1페이지로 유지

        // 4. endPage가 실제 마지막 페이지보다 크면 보정
        if (this.endPage > lastPage) {
            this.endPage = lastPage;
        }

        // 5. 이전/다음 버튼 활성화 여부 및 이동할 페이지 번호 계산
        this.prev = this.startPage > 1;
        this.next = total > (long) (this.startPage + blockSize - 1) * size;

        // "이전" 누르면 이전 블록의 마지막 페이지로 (예: 6~10페이지에서 이전 누르면 5페이지)
        this.prevPage = this.prev ? this.startPage - 1 : 1;

        // "다음" 누르면 다음 블록의 첫 페이지로 (예: 1~5페이지에서 다음 누르면 6페이지, 6~10에서 누르면 11페이지)
        this.nextPage = this.next ? this.endPage + 1 : lastPage;
    }
}