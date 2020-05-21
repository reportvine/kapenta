import Vue from 'vue';
import VueRouter from 'vue-router';
import App from './App.vue';
import ReportsIndex from './components/ReportsIndex.vue';

Vue.config.productionTip = false;

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
