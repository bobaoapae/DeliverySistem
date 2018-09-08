
$(document).ready(function () {
    $("#sidebarCollapse").on("click", function () {
        $("#sidebar").toggleClass("active");
        $(this).toggleClass("active");
        $("#content").css("maxWidth", ($(".wrapper").width() - ($("#sidebar").hasClass("active") ? 250 : 0)) + "px");
        window.setTimeout(() => {
            $("#topVendidos").highcharts().reflow();
        }, 300);
    });
    $("#content").css("maxWidth", ($(".wrapper").width() - ($("#sidebar").css("margin-left") == "-250px" ? 0 : 250)) + "px");
    window.setTimeout(() => {
        $("#topVendidos").highcharts().reflow();
    }, 300);
});

$(window).resize(function () {
    $("#content").css("maxWidth", ($(".wrapper").width() - ($("#sidebar").css("margin-left") == "-250px" ? 0 : 250)) + "px");
    window.setTimeout(() => {
        $("#topVendidos").highcharts().reflow();
    }, 300);
});
