<template>
  <q-dialog persistent vmodel="model-value" @before-show="resetData">
    <q-card class="q-gutter-md">
      <q-card-section class="text-h6">
        Add New Payment
      </q-card-section>
      <q-card-section class="q-gutter-md">
        <q-input outlined label="Amount" v-model="data.amount" prefix="$"/>
        <q-select outlined label="Payment Method" :options="methodOptions" v-model="data.method"/>
        <q-select outlined label="Payment Status" :options="statusOptions" v-model="data.status"/>
        <q-input outlined label="Notes" v-model="data.notes"/>
      </q-card-section>
      <q-separator/>
      <q-card-actions>
        <q-btn v-close-popup color="primary" label="Save" type="submit" @click="onSave"/>
        <q-btn v-close-popup color="primary" label="Cancel" type="reset" flat @click="onCancel"/>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script>
import {ref} from 'vue';

export default {
  name: "PaymentDialog",
  methods: {
    resetData() {
      console.log("Resetting payment data")
      console.log(`   Provided: ${JSON.stringify(this.payment)}`)

      this.data.index = this.payment.index;
      this.data.amount = this.payment.amount || 0;
      this.data.method = this.payment.method || 'CHECK';
      this.data.status = this.payment.status || 'SUCCESSFUL';
      this.data.notes = this.payment.notes || '';
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
      data: ref({
        amount: 0,
        method: 'CHECK',
        status: 'SUCCESSFUL',
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
