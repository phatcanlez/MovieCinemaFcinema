document.addEventListener('DOMContentLoaded', () => {
    const employeeTableBody = document.getElementById('employeeTableBody');
    const noResultsRow = document.getElementById('noResultsRow');
    const addEmployeeButton = document.getElementById('addEmployeeButton');
    const saveAddEmployeeButton = document.getElementById('saveAddEmployeeButton');
    const saveEditEmployeeButton = document.getElementById('saveEditEmployeeButton');

    // Sample data with corrected fields
    let employees = [
        {
            id: 1,
            userName: 'nguyenvana',
            fullName: 'Nguyen Van A',
            dateOfBirth: '1990-01-15',
            gender: 'Male',
            email: 'nguyenvana@example.com',
            identityCard: '123456789',
            phone: '0935335515',
            address: 'District 1, Ho Chi Minh City',
            registerDate: '2023-01-10'
        },
        {
            id: 2,
            userName: 'tranvanb',
            fullName: 'Tran Van B',
            dateOfBirth: '1992-03-22',
            gender: 'Male',
            email: 'tranvanb@example.com',
            identityCard: '987654321',
            phone: '0935335516',
            address: 'District 3, Ho Chi Minh City',
            registerDate: '2023-02-15'
        },
        {
            id: 3,
            userName: 'lethic',
            fullName: 'Le Thi C',
            dateOfBirth: '1995-07-30',
            gender: 'Female',
            email: 'lethic@example.com',
            identityCard: '456789123',
            phone: '0935335517',
            address: 'District 7, Ho Chi Minh City',
            registerDate: '2023-03-20'
        },
        {
            id: 4,
            userName: 'phamvand',
            fullName: 'Pham Van D',
            dateOfBirth: '1988-11-05',
            gender: 'Male',
            email: 'phamvand@example.com',
            identityCard: '321654987',
            phone: '0935335518',
            address: 'District 5, Ho Chi Minh City',
            registerDate: '2023-04-25'
        },
        {
            id: 5,
            userName: 'nguyenthie',
            fullName: 'Nguyen Thi E',
            dateOfBirth: '1993-09-12',
            gender: 'Female',
            email: 'nguyenthie@example.com',
            identityCard: '654987321',
            phone: '0935335519',
            address: 'District 10, Ho Chi Minh City',
            registerDate: '2023-05-30'
        }
    ];

    // Populate table
    function populateTable(data) {
        employeeTableBody.innerHTML = '';
        // Add no results row back
        employeeTableBody.appendChild(noResultsRow);
        if (data.length === 0) {
            noResultsRow.style.display = 'table-row';
            return;
        }
        noResultsRow.style.display = 'none';
        data.forEach((account, index) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${index + 1}</td>
                <td>${account.userName}</td>
                <td>${account.fullName}</td>
                <td>${account.dateOfBirth}</td>
                <td>${account.gender}</td>
                <td>${account.email}</td>
                <td>${account.identityCard}</td>
                <td>${account.phone}</td>
                <td>${account.address}</td>
                <td>${account.registerDate}</td>
                <td><button class="btn btn-warning btn-sm edit-btn me-2" data-index="${index}"><i class="fas fa-edit"></i></button></td>
                <td><button class="btn btn-danger btn-sm delete-btn" data-index="${index}"><i class="fas fa-trash"></i></button></td>
            `;
            employeeTableBody.appendChild(row);
        });

        // Search functionality
        searchButton.addEventListener('click', () => {
            const keyword = searchInput.value.toLowerCase();
            const filteredAccounts = employees.filter(account =>
                account.userName.toLowerCase().includes(keyword) ||
                account.fullName.toLowerCase().includes(keyword) ||
                account.phone.includes(keyword) ||
                account.identityCard.includes(keyword)
            );
            populateTable(filteredAccounts);
        });

        // Add event listeners to edit and delete buttons
        document.querySelectorAll('.edit-btn').forEach(btn => {
            btn.addEventListener('click', () => showEditModal(btn.getAttribute('data-index')));
        });
        document.querySelectorAll('.delete-btn').forEach(btn => {
            btn.addEventListener('click', () => confirmDelete(btn.getAttribute('data-index')));
        });
    }

    // Show add employee modal
    addEmployeeButton.addEventListener('click', () => {
        const modal = new bootstrap.Modal(document.getElementById('addEmployeeModal'));
        modal.show();
    });

    // Save new employee
    saveAddEmployeeButton.addEventListener('click', () => {
        const username = document.getElementById('addUsername').value.trim();
        const fullName = document.getElementById('addFullName').value.trim();
        const dateOfBirth = document.getElementById('addDob').value;
        const gender = document.getElementById('addGender').value;
        const email = document.getElementById('addEmail').value.trim();
        const identityCard = document.getElementById('addIdentityCard').value.trim();
        const phone = document.getElementById('addPhone').value.trim();
        const address = document.getElementById('addAddress').value.trim();
        const registerDate = document.getElementById('addRegisterDate').value;

        if (username && fullName && dateOfBirth && gender && email && identityCard && phone && address && registerDate) {
            const newEmployee = {
                id: employees.length + 1,
                username,
                fullName,
                dateOfBirth,
                gender,
                email,
                identityCard,
                phone,
                address,
                registerDate
            };
            employees.push(newEmployee);
            populateTable(employees);
            bootstrap.Modal.getInstance(document.getElementById('addEmployeeModal')).hide();
            // Clear form
            document.getElementById('addUsername').value = '';
            document.getElementById('addFullName').value = '';
            document.getElementById('addDob').value = '';
            document.getElementById('addGender').value = 'Male';
            document.getElementById('addEmail').value = '';
            document.getElementById('addIdentityCard').value = '';
            document.getElementById('addPhone').value = '';
            document.getElementById('addAddress').value = '';
            document.getElementById('addRegisterDate').value = '';
        } else {
            alert('Please fill in all fields correctly.');
        }
    });

    // Show edit employee modal with auto-populated data
    function showEditModal(index) {
        const employee = employees[index];
        document.getElementById('editEmployeeIndex').value = index;
        document.getElementById('editUsername').value = employee.username;
        document.getElementById('editFullName').value = employee.fullName;
        document.getElementById('editDob').value = employee.dateOfBirth;
        document.getElementById('editGender').value = employee.gender;
        document.getElementById('editEmail').value = employee.email;
        document.getElementById('editIdentityCard').value = employee.identityCard;
        document.getElementById('editPhone').value = employee.phone;
        document.getElementById('editAddress').value = employee.address;
        document.getElementById('editRegisterDate').value = employee.registerDate;

        const modal = new bootstrap.Modal(document.getElementById('editEmployeeModal'));
        modal.show();
    }

    // Save edited employee
    saveEditEmployeeButton.addEventListener('click', () => {
        const index = document.getElementById('editEmployeeIndex').value;
        const fullName = document.getElementById('editFullName').value.trim();
        const dateOfBirth = document.getElementById('editDob').value;
        const gender = document.getElementById('editGender').value;
        const email = document.getElementById('editEmail').value.trim();
        const identityCard = document.getElementById('editIdentityCard').value.trim();
        const phone = document.getElementById('editPhone').value.trim();
        const address = document.getElementById('editAddress').value.trim();
        const registerDate = document.getElementById('editRegisterDate').value;

        if (fullName && dateOfBirth && gender && email && identityCard && phone && address && registerDate) {
            employees[index] = {
                ...employees[index],
                fullName,
                dateOfBirth,
                gender,
                email,
                identityCard,
                phone,
                address,
                registerDate
            };
            populateTable(employees);
            bootstrap.Modal.getInstance(document.getElementById('editEmployeeModal')).hide();
        } else {
            alert('Please fill in all fields correctly.');
        }
    });

    // Confirm and delete employee
    function confirmDelete(index) {
        const employee = employees[index];
        if (confirm(`Are you sure you want to delete ${employee.fullName}?`)) {
            employees.splice(index, 1);
            // Reassign IDs to maintain sequential numbering
            employees = employees.map((emp, i) => ({ ...emp, id: i + 1 }));
            populateTable(employees);
        }
    }

    // Initial table population
    populateTable(employees);
});