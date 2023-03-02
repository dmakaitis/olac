<template>
  <q-dialog persistent vmodel="model-value" @before-show="resetData">
    <q-card class="q-gutter-md">
      <q-card-section class="text-h6">
        Add New Payment
      </q-card-section>
      <q-card-section class="q-gutter-md">
        <q-input outlined label="Amount" v-model="data.amount" prefix="$"/>
        <q-select outlined label="Payment Method" :options="methodOptions" v-model="data.paymentMethod"/>
        <q-select outlined label="Payment Status" :options="statusOptions" v-model="data.paymentStatus"/>
        <q-input outlined label="Notes" v-model="data.notes"/>
      </q-card-section>
      <q-separator/>
      <q-card-actions>
        <q-btn v-close-popup flat color="primary" label="Save" @click="onSave"/>
        <q-btn v-close-popup flat color="primary" label="Cancel" @click="onCancel"/>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script>
import {reactive, ref} from 'vue';

export default {
  name: "PaymentDialog",
  methods: {
    resetData() {
      console.log("Resetting payment data")
      console.log(`   Provided: ${JSON.stringify(this.payment)}`)

      this.data.amount = this.payment.value.amount || 0;
      this.data.paymentMethod = this.payment.value.method || 'CHECK';
      this.data.paymentStatus = this.payment.value.status || 'SUCCESSFUL';
      this.data.notes = this.payment.value.notes || '';
    },
    onSave() {
      this.$emit('save', this.data);
    },
    onCancel() {
      this.$emit('cancel');
    }
  },
  props: [
    'payment'
  ],
  setup(props) {
    return {
      modelValue: ref(false),
      data: reactive({
        amount: 0,
        paymentMethod: 'CHECK',
        paymentStatus: 'SUCCESSFUL',
        notes: '',
        payment: props.payment
      }),
      methodOptions: ['ONLINE', 'CHECK', 'COMP'],
      statusOptions: ['PENDING', 'SUCCESSFUL', 'FAILED']
    }
  }
}
</script>

<style scoped>

</style>
