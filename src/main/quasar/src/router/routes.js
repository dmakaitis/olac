import AdminLayout from "layouts/AdminLayout.vue";
import AdminTicketTypes from "pages/AdminTicketTypes.vue";
import AdminReservations from "pages/AdminReservations.vue";

const routes = [
  {
    path: '/',
    component: AdminLayout,
    children: [
      {
        path: 'ticket-types',
        component: AdminTicketTypes
      },
      {
        path: 'reservations',
        component: AdminReservations
      }
    ]
  }
  // {
  //   path: '/admin/',
  //   component: () => import('layouts/MainLayout.vue'),
  //   children: [
  //     { path: '', component: () => import('pages/IndexPage.vue') }
  //   ]
  // },

  // Always leave this as last one,
  // but you can also remove it
  // {
  //   path: '/:catchAll(.*)*',
  //   component: () => import('pages/ErrorNotFound.vue')
  // }
]

export default routes
