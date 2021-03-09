import Vue from 'vue'
import { BootstrapVue, IconsPlugin } from 'bootstrap-vue'
import VueRouter from 'vue-router';
import App from './App.vue';
import ReportsIndex from './components/ReportsIndex.vue';

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'


Vue.use(BootstrapVue)
Vue.use(IconsPlugin)

Vue.config.productionTip = false;
Vue.use(VueRouter)

const router = new VueRouter({
  routes: [
    {
      path: '/',
      component: ReportsIndex
    },
    {
      path: '/reports',
      component: ReportsIndex
    },
    {
      path: '*',
      component: ReportsIndex
    }
  ]
})

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
