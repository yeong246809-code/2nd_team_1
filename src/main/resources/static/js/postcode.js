/**
 * 카카오/다음 우편번호 찾기
 */
function Postcode(zipId, addr1Id, addr2Id) {
    const postcodeApi = window.daum?.Postcode || window.kakao?.Postcode;

    if (!postcodeApi) {
        alert('우편번호 검색 스크립트를 불러오지 못했습니다.');
        return;
    }

    const zipInput = document.getElementById(zipId || 'zip');
    const addr1Input = document.getElementById(addr1Id || 'addr1');
    const addr2Input = document.getElementById(addr2Id || 'addr2');

    if (!zipInput || !addr1Input || !addr2Input) {
        alert('주소 입력칸을 찾을 수 없습니다.');
        return;
    }

    new postcodeApi({
        oncomplete: function(data) {
            let addr = '';
            let extraAddr = '';

            if (data.userSelectedType === 'R') {
                addr = data.roadAddress;
            } else {
                addr = data.jibunAddress;
            }

            if (data.userSelectedType === 'R') {
                if (data.bname !== '' && /[동로가]$/.test(data.bname)) {
                    extraAddr += data.bname;
                }
                if (data.buildingName !== '' && data.apartment === 'Y') {
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                if (extraAddr !== '') {
                    addr += ' (' + extraAddr + ')';
                }
            }

            zipInput.value = data.zonecode;
            addr1Input.value = addr;
            addr2Input.focus();
        }
    }).open();
}
