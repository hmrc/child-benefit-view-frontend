document.addEventListener('DOMContentLoaded', function(event) {
    var printLink = document.querySelector('.print-link');
    if (printLink !== null) {
        printLink.addEventListener('click', function(e){
            e.preventDefault();
            e.stopPropagation();
            window.print();
        });
    }
});
