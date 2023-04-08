<template>
  <q-dialog vmodel="model-value">
    <q-card>
      <q-card-section>
        <div class="text-h6">Payment Failed</div>
      </q-card-section>

      <q-card-section class="q-pt-none">
        We have encountered an error while trying to process your online payment. Please try again later.
      </q-card-section>

      <q-card-section class="q-pt-none">
        If the problem persists, you may select the option to pay by check and send in your payment by mail.
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="OK" color="primary" v-close-popup/>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script>
import {ref} from "vue";

export default {
  name: "PaymentErrorDialog",

  setup() {
    return {
      modelValue: ref(true)
    }
  }
}
</script>

<style scoped>

</style>
