<template>
  <q-page padding class="bg-white q-gutter-md">
    <q-table ref="table" title="Reservations" :rows="state.rows" :columns="columns" row-key="reservationId"
             @row-click="onRowClick"
             :selection="isAdmin() ? 'single' : 'none'" v-model:selected="selected" v-model:pagination="pagination"
             :loading="loading" :filter="filter" @request="onTableRefresh">
      <template v-slot:top-left>
        <span class="q-table__title">{{ this.table.title }}</span><br/>
        <q-input borderless dense debounce="300" placeholder="Search" v-model="filter">
          <template v-slot:prepend>
            <q-icon name="search"/>
          </template>
        </q-input>
      </template>
      <template v-slot:top-right>
        <div class="text-right">
          <q-btn color="primary" icon-right="archive" label="Export to CSV" no-caps @click="onExportTable"/>
          <br/>
          (export using current search and sort settings)
        </div>
      </template>
      <template v-slot:bottom-row>
        <q-tr>
          <q-td></q-td>
          <q-td></q-td>
          <q-td></q-td>
          <q-td></q-td>
          <q-td></q-td>
          <q-td></q-td>
          <q-td></q-td>
          <q-td class="text-weight-bold text-right">Total:</q-td>
          <q-td class="text-weight-bold text-center">{{ state.stats.ticketsReserved }}</q-td>
          <q-td class="text-weight-bold text-right">{{ currency(state.stats.amountDue) }}</q-td>
          <q-td class="text-weight-bold text-right">{{ currency(state.stats.amountPaid) }}</q-td>
          <q-td v-for="col in columns" :key="col" props="col">{{ col.name }}</q-td>
        </q-tr>
      </template>
    </q-table>
    <q-btn label="New Reservation" @click="onNewReservation"/>
    <q-btn v-if="isAdmin()" label="Delete Selected Reservation" @click="onDelete"/>
  </q-page>

  <ReservationDialog :reservation="detail.row" :ticket-types="state.ticketTypes"
                     v-model="showDetail" @save="onSaveReservation" @cancel="onCancel" @edit-payment="onEditPayment"/>
  <PaymentDialog :payment="selectedPayment" v-model="showPaymentDialog" @save="onSavePayment"
                 @cancel="onCancelPayment"/>
  <ConfirmationDialog v-model="confirmDelete" @yes="onConfirmDelete">
    Are you sure you want to delete reservation number <b>{{ selected[0].id }}</b> for <b>{{ selected[0].firstName }}
    {{ selected[0].lastName }}</b>'?
  </ConfirmationDialog>
</template>

<script>
import {reactive, ref} from 'vue';
import {currency} from "boot/helper";
import {api} from 'boot/axios.js';
import ReservationDialog from "components/ReservationDialog.vue";
import PaymentDialog from "components/PaymentDialog.vue";
import {useStore} from "vuex";
import {date, exportFile, useQuasar} from 'quasar'
import ConfirmationDialog from "components/ConfirmationDialog.vue";

const columns = [
  {name: 'id', label: 'Reservation Number', field: row => row.id, align: 'left', sortable: true},
  {
    name: 'reservationTimestamp',
    label: 'Date/Time Reserved',
    field: row => row.reservationTimestamp,
    align: 'left',
    format: val => date.formatDate(val, 'MMM D, YYYY HH:mm:ss'),
    sortable: true
  },
  {name: 'first-name', label: 'First Name', field: row => row.firstName, align: 'left'},
  {name: 'lastName', label: 'Last Name', field: row => row.lastName, align: 'left', sortable: true},
  {name: 'email', label: 'Email', field: row => row.email, align: 'left', sortable: true},
  {name: 'phone', label: 'Phone', field: row => row.phone, align: 'left'},
  {name: 'status', label: 'Status', field: row => row.status, align: 'left', sortable: true},
  {
    name: 'tickets',
    label: 'Tickets',
    field: row => row.ticketCounts,
    align: 'center',
    format: val => `${val.reduce((a, b) => a + b.count, 0)}`
  },
  {name: 'amount-due', label: 'Amount Due', field: row => row.amountDue, format: val => `${currency(val)}`},
  {
    name: 'amount-paid',
    label: 'Amount Paid',
    field: row => row.payments,
    format: val => `${currency(val.reduce((a, b) => a + parseFloat(b.amount), 0.0))}`
  }
];

export default {
  name: 'AdminReservations',
  components: {ReservationDialog, PaymentDialog, ConfirmationDialog},
  methods: {
    onRowClick(event, row, index) {
      this.detail.row = row;

      this.state.ticketTypes.forEach(type => {
        type.count = 0;

        let count = this.detail.row.ticketCounts.find(c => c.ticketTypeCode === type.code)
        if (count) {
          type.count = count.count;
        }
      });

      this.showDetail = true
    },
    onNewReservation() {
      this.detail.row = {
        id: '',
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        status: 'PENDING_PAYMENT',
        payments: [],
        ticketCounts: []
      }

      this.state.ticketTypes.forEach(type => {
        type.count = 0
      })

      this.showDetail = true
    },
    onCancel() {
      this.loadReservations(this.pagination);
      this.showDetail = false;
    },
    onDelete() {
      this.confirmDelete = true
    },
    onConfirmDelete() {
      this.confirmDelete = false
      api.delete(`/api/admin/reservations/${this.selected[0].reservationId}`)
        .then(response => this.loadReservations(this.pagination))
        .catch(error => alert(error))
    },
    loadReservations(pagination) {
      this.loading = true

      let {page, rowsPerPage, sortBy, descending} = pagination

      api.get(`/api/event/reservations?page=${page - 1}&perPage=${rowsPerPage}&sortBy=${sortBy}&desc=${descending}&filter=${this.filter}`)
        .then(response => {
          this.state.rows = response.data.data
          this.pagination.rowsNumber = response.data.totalItems
          this.pagination.page = response.data.pageNumber + 1
          this.pagination.rowsPerPage = response.data.itemsPerPage
          this.pagination.sortBy = response.data.sortBy
          this.pagination.descending = response.data.descending
          this.state.stats = response.data.ext

          this.loading = false
        })
        .catch(error => alert(error))
    },
    loadTicketTypeData() {
      api.get('/api/public/ticket-types')
        .then(response => {
          this.state.ticketTypes = response.data;
          this.state.ticketTypes.forEach(type => type.count = 0)
        })
        .catch(error => alert(error))
    },
    onSaveReservation(reservationData) {
      reservationData.ticketCounts = this.state.ticketTypes
        .filter(t => t.count > 0)
        .map(t => {
          return {
            ticketTypeCode: t.code,
            count: parseInt(t.count)
          }
        });
      reservationData.amountDue = this.state.ticketTypes
        .map(t => t.count * t.costPerTicket)
        .reduce((t, n) => t + n);

      if (reservationData.id == '') {
        reservationData.id = null
      }

      api.put(`/api/event/reservations/${this.detail.row.reservationId}`, reservationData)
        .then(response => this.loadReservations(this.pagination))
        .catch(error => alert(error));

      this.showDetail = false;
    },
    onEditPayment(data) {
      this.selectedPayment = data
      this.showPaymentDialog = true
    },
    onCancelPayment() {
      this.showPaymentDialog = false;
    },
    onSavePayment(data) {
      if (data.index >= 0) {
        this.detail.row.payments[data.index].amount = data.amount;
        this.detail.row.payments[data.index].status = data.status;
        this.detail.row.payments[data.index].method = data.method;
        this.detail.row.payments[data.index].notes = data.notes;
      } else {
        this.detail.row.payments.push({
          amount: data.amount,
          status: data.status,
          method: data.method,
          notes: data.notes
        });
      }
      this.showPaymentDialog = false;
    },
    isAdmin() {
      return this.store.getters['auth/isAdmin']
    },
    onTableRefresh(props) {
      this.loadReservations(props.pagination)
    },
    onExportTable() {
      console.log("Exporting table...")

      let {sortBy, descending} = this.pagination

      api.get(`/api/event/reservations.csv?sortBy=${sortBy}&desc=${descending}&filter=${this.filter}`)
        .then(response => {
          const status = exportFile('reservations.csv', response.data, 'text/csv')

          if (status !== true) {
            useQuasar().notify({
              message: "Browser denied file download",
              color: "negative",
              icon: "warning"
            })
          }
        })
    }
  },
  setup() {
    const store = useStore()
    const state = reactive({
      rows: [],
      ticketTypes: [],
      stats: {}
    })

    return {
      store,
      columns,
      state,
      table: ref({}),
      showDetail: ref(false),
      detail: reactive({}),
      selected: ref([]),
      selectedPayment: ref({}),
      showPaymentDialog: ref(false),
      confirmDelete: ref(false),
      loading: ref(false),
      pagination: ref({
        sortBy: 'reservationTimestamp',
        descending: true,
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      }),
      filter: ref(''),
      currency
    }
  },
  mounted() {
    this.loadReservations(this.pagination);
    this.loadTicketTypeData();
  }
}
</script>
