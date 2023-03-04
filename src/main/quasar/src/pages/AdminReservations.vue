<template>
  <q-page padding class="q-gutter-md">
    <q-table title="Reservations" :rows="state.rows" :columns="columns" row-key="reservationId" @row-click="onRowClick"
             selection="single" v-model:selected="selected">
    </q-table>
    <q-btn label="New Reservation"/>
    <q-btn label="Delete Selected Reservation"/>
  </q-page>

  <ReservationDialog :reservation="detail.row" :ticket-types="state.ticketTypes"
                     v-model="showDetail" @save="onSaveReservation" @cancel="onCancel" @edit-payment="onEditPayment"/>
  <PaymentDialog :payment="selectedPayment" v-model="showPaymentDialog" @save="onSavePayment"
                 @cancel="onCancelPayment"/>
</template>

<script>
import {reactive, ref} from 'vue';
import {currency} from "boot/helper";
import {api} from 'boot/axios.js';
import ReservationDialog from "components/ReservationDialog.vue";
import PaymentDialog from "components/PaymentDialog.vue";

const columns = [
  {name: 'id', label: 'Reservation Number', field: row => row.id, align: 'left', sortable: true},
  {name: 'first-name', label: 'First Name', field: row => row.firstName, align: 'left'},
  {name: 'last-name', label: 'Last Name', field: row => row.lastName, align: 'left', sortable: true},
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
  components: {ReservationDialog, PaymentDialog},
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
    onCancel() {
      this.loadReservations();
      this.showDetail = false;
    },
    loadReservations() {
      api.get('/api/admin/reservations')
        .then(response => this.state.rows = response.data)
        .catch(error => alert(error))
    },
    loadTicketTypeData() {
      api.get('/api/admin/ticket-types')
        .then(response => {
          this.state.ticketTypes = response.data;
          this.state.ticketTypes.forEach(type => type.count = 0)
          console.log("Ticket types loaded...")
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

      console.log(`Saving reservation: ${JSON.stringify(reservationData)}`);
      api.put(`/api/admin/reservations/${this.detail.row.reservationId}`, reservationData)
        .then(response => this.loadReservations())
        .catch(error => alert(error));

      this.showDetail = false;
    },
    onEditPayment(data) {
      console.log(`Received edit payment event: ${JSON.stringify(data)}`)
      this.selectedPayment = data
      this.showPaymentDialog = true
    },
    onCancelPayment() {
      this.showPaymentDialog = false;
    },
    onSavePayment(data) {
      console.log(`Saving payment: ${JSON.stringify(data)}`);
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
    }
  },
  setup() {
    const state = reactive({rows: [], ticketTypes: []})

    return {
      columns,
      state,
      showDetail: ref(false),
      detail: reactive({}),
      selected: ref([]),
      selectedPayment: ref({}),
      showPaymentDialog: ref(false)
    }
  },
  mounted() {
    this.loadReservations();
    this.loadTicketTypeData();
  }
}
</script>
