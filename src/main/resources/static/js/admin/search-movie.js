
    document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.getElementById("searchInput");

    function normalizeVietnamese(str) {
    return str
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase();
}

    function similarity(s1, s2) {
    const longer = s1.length > s2.length ? s1 : s2;
    const shorter = s1.length > s2.length ? s2 : s1;
    const longerLength = longer.length;
    if (longerLength === 0) return 1.0;
    const inter = longerLength - editDistance(longer, shorter);
    return inter / longerLength;
}

    function editDistance(s1, s2) {
    const costs = [];
    for (let i = 0; i <= s1.length; i++) {
    let lastValue = i;
    for (let j = 0; j <= s2.length; j++) {
    if (i === 0) costs[j] = j;
    else {
    if (j > 0) {
    let newValue = costs[j - 1];
    if (s1.charAt(i - 1) !== s2.charAt(j - 1))
    newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
    costs[j - 1] = lastValue;
    lastValue = newValue;
}
}
}
    if (i > 0) costs[s2.length] = lastValue;
}
    return costs[s2.length];
}

    searchInput.addEventListener("input", function () {
    const keyword = normalizeVietnamese(searchInput.value.trim());
    const tableRows = document.querySelectorAll("tbody tr");
    let hasResult = false;

    tableRows.forEach((row) => {
    const nameCell = row.querySelector("td:nth-child(3) span:last-child");
    if (!nameCell) return;

    const movieName = normalizeVietnamese(nameCell.innerText.trim());
    const isMatch = movieName.includes(keyword) || similarity(movieName, keyword) >= 0.6;

    row.style.display = isMatch ? "" : "none";
    if (isMatch) hasResult = true;
});

    const noResultRow = document.querySelector('tr[th\\:unless]');
    if (noResultRow) {
    noResultRow.style.display = hasResult ? "none" : "";
}
});
});

