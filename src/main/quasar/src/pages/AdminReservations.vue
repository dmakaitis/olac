<template>
  <q-page padding>
    <q-table title="Reservations" :rows="state.rows" :columns="columns" row-key="code">
    </q-table>
  </q-page>
</template>

<script>
import {reactive} from 'vue'

const columns = [
  {
    name: 'id',
    required: false,
    label: 'Reservation Number',
    align: 'left',
    field: row => row.id,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'reservation-id',
    required: true,
    label: 'Reservation ID',
    align: 'left',
    field: row => row.reservationId,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'first-name',
    required: true,
    label: 'First Name',
    align: 'left',
    field: row => row.firstName,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'last-name',
    required: true,
    label: 'Last Name',
    align: 'left',
    field: row => row.lastName,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'email',
    required: true,
    label: 'Email',
    align: 'left',
    field: row => row.email,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'phone',
    required: true,
    label: 'Phone',
    align: 'left',
    field: row => row.phone,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'status',
    required: true,
    label: 'Status',
    align: 'left',
    field: row => row.status,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'timestamp',
    required: true,
    label: 'Timestamp',
    align: 'left',
    field: row => row.reservationTimestamp,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'tickets',
    required: true,
    label: 'Tickets',
    align: 'center',
    field: row => row.ticketCounts,
    format: val => `${val.reduce((a, b) => a + b.count, 0)}`,
    sortable: false
  },
  {
    name: 'amount-due',
    required: true,
    label: 'Amount Due',
    align: 'right',
    field: row => row.amountDue,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'amount-paid',
    required: true,
    label: 'Amount Due',
    align: 'right',
    field: row => row.payments,
    format: val => `${val.reduce((a, b) => a + b.amount, 0.0)}`,
    sortable: false
  }
];


export default {
  name: 'AdminTicketTypes',
  setup() {
    const state = reactive({rows: []})

    function loadTicketTypeData() {
      fetch("/api/admin/reservations", {method: 'GET'})
        .then(response => response.json())
        .then(data => (state.rows = data))
        .catch(err => {
          console.log(err);
        });
    }

    loadTicketTypeData();

    return {
      columns,
      state
    }
  }
}
</script>
