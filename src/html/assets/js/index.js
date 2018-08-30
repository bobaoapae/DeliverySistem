
$(document).ready(function () {
    $("#sidebarCollapse").on("click", function () {
        $("#sidebar").toggleClass("active");
        $(this).toggleClass("active");
    });
    $("#content").css("maxWidth", ($(".wrapper").width() - 250) + "px");
    window.setTimeout(() => {
        $("#topVendidos").highcharts().reflow();
    }, 300);
});

$(window).resize(function () {
    $("#content").css("maxWidth", ($(".wrapper").width() - 250) + "px");
    window.setTimeout(() => {
        $("#topVendidos").highcharts().reflow();
    }, 300);
});
