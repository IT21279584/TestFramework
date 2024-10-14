document.addEventListener('DOMContentLoaded', function() {
    // Select the <p> element inside .sysenv-container .card-header
    var systemHeading = document.querySelector('.sysenv-container .card-header p');

    // Debugging - check if element is found
    console.log(systemHeading);

    // Ensure the element exists and has the text 'System/Environment'
    if (systemHeading && systemHeading.textContent.trim() === 'System/Environment') {
        console.log('Changing text from System/Environment to System Requirements');
        // Replace 'System/Environment' with 'System Requirements'
        systemHeading.textContent = 'System Requirements';
    } else {
        console.log('Element not found or text not matching');
    }
});
