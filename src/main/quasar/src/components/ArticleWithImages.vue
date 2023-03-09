<template>
  <q-card>
    <q-card-section>
      <div v-if="title" class="text-h4">{{ title }}</div>
      <div v-if="subtitle" class="text-h5">{{ subtitle }}</div>
    </q-card-section>
    <q-card-section>
      <div v-if="leftImagesArray.length" class="float-left">
        <q-img v-for="image in leftImagesArray" :key="image" :src="image" class="shadow-24"/>
      </div>
      <div v-if="rightImagesArray.length" class="float-right">
        <q-img v-for="image in rightImagesArray" :key="image" :src="image" class="shadow-24"/>
      </div>
      <slot></slot>
      <div style="clear: both;"></div>
    </q-card-section>
  </q-card>
</template>

<script>
import {ref} from "vue";

export default {
  name: "ArticleWithImages",
  props: {
    title: String,
    subtitle: String,
    leftImages: Array,
    rightImages: Array
  },
  setup(props) {
    return {
      leftImagesArray: ref([]),
      rightImagesArray: ref([])
    }
  },
  mounted() {
    if (this.leftImages instanceof Array) {
      this.leftImagesArray = this.leftImages
    } else {
      if (this.leftImages) {
        this.leftImagesArray = [this.leftImages]
      } else {
        this.leftImagesArray = []
      }
    }

    if (this.rightImages instanceof Array) {
      this.rightImagesArray = this.rightImages
    } else {
      if (this.rightImages) {
        this.rightImagesArray = [this.rightImages]
      } else {
        this.rightImagesArray = []
      }
    }
  }
}
</script>

<style scoped>
.float-left {
  float: left;
  margin: 15px;
  width: 30%;
  min-width: 300px;
}

.float-right {
  float: right;
  margin: 15px;
  width: 30%;
  min-width: 300px;
}

@media (max-width: 600px) {
  .float-left {
    float: none;
    width: 100%;
    min-width: 100%;
  }

  .float-right {
    float: none;
    width: 100%;
    min-width: 100%;
  }
}
</style>
