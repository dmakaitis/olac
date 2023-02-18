<template>
  <q-page padding>
    <q-table title="Ticket Types" :rows="state.rows" :columns="columns" row-key="code">
    </q-table>
  </q-page>
</template>

<script>
import {reactive} from 'vue'

const columns = [
  {
    name: 'code',
    required: false,
    label: 'Type Code',
    align: 'left',
    field: row => row.code,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'description',
    required: true,
    label: 'Description',
    align: 'left',
    field: row => row.description,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'costPerTicket',
    required: true,
    label: 'Cost per Ticket',
    align: 'right',
    field: row => row.costPerTicket,
    format: val => `${val}`,
    sortable: false
  }
];


export default {
  name: 'AdminTicketTypes',
  setup() {
    const state = reactive({rows: []})

    function loadTicketTypeData() {
      fetch("/api/admin/ticket-types", {method: 'GET'})
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
