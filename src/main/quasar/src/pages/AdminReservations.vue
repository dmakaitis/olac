<template>
  <q-page padding class="q-gutter-md">
    <q-table title="Reservations" :rows="state.rows" :columns="columns" row-key="reservationId" @row-click="onRowClick"
             selection="single" v-model:selected="selected">
    </q-table>
    <q-btn label="New Reservation"/>
    <q-btn label="Delete Selected Reservation"/>
  </q-page>

  <q-dialog v-model="showDetail">
    <q-card>
      <div class="q-pa-md">
        <q-card-section>
          <b>Reservation</b>
        </q-card-section>
        <q-card-section>
          <q-form class="q-gutter-md" @reset="onCancel">
            <div>
              <q-btn label="Save" type="submit" color="primary"/>
              <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm"/>
            </div>
          </q-form>
        </q-card-section>
      </div>
    </q-card>
  </q-dialog>
</template>

<script>
import {reactive, ref} from 'vue'
import {useStore} from 'vuex'
import {currency} from "boot/helper";
import {api} from 'boot/axios.js'

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
    sortable: false
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
    sortable: true
  },
  {
    name: 'email',
    required: true,
    label: 'Email',
    align: 'left',
    field: row => row.email,
    format: val => `${val}`,
    sortable: true
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
    sortable: true
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
    format: val => `${currency(val)}`,
    sortable: false
  },
  {
    name: 'amount-paid',
    required: true,
    label: 'Amount Due',
    align: 'right',
    field: row => row.payments,
    format: val => `${currency(val.reduce((a, b) => a + b.amount, 0.0))}`,
    sortable: false
  }
];


export default {
  name: 'AdminTicketTypes',
  methods: {
    onRowClick(event, row, index) {
      this.showDetail = true;
    },
    onCancel() {
      this.showDetail = false;
    },
    loadTicketTypeData() {
      api.get('/api/admin/reservations')
        .then(response => this.state.rows = response.data)
        .catch(error => alert(error))
    }
  },
  setup() {
    const store = useStore()
    const state = reactive({rows: []})

    return {
      store,
      columns,
      state,
      showDetail: ref(false),
      selected: ref([])
    }
  },
  mounted() {
    this.loadTicketTypeData();
  }
}
</script>
