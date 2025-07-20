import component from "element-plus/es/components/tree-select/src/tree-select-option.mjs";
import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
    meta: {
      title: "Login - Restaurant Reservation"
    }
  },
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/Home.vue"),
    meta: {
      title: "Home - Restaurant Reservation",
      requiresAuth: true
    }
  },
  {
    path: "/restaurant/:id",
    name: "RestaurantDetail",
    component: () => import("@/views/RestaurantDetail.vue"),
    meta: {
      title: "Restaurant Detail",
      requiresAuth: true
    }
  },
  {
    path: "/profile",
    name: "Profile",
    component: () => import("@/views/Profile.vue"),
    meta: {
      title: "My Profile",
      requiresAuth: true
    }
  },
  {
    path: "/reservations",
    name: "Reservations",
    component: () => import("@/views/Reservations.vue"),
    meta: {
      title: "My Reservations",
      requiresAuth: true
    }
  },
  {
    path: "/explore",
    name: "Explore",
    component: () => import("@/views/Explore.vue"),
    meta: {
      title: "Explore",
      requiresAuth: true
    }
  },
  {
    path: "/favorites",
    name: "Favorites",
    component: () => import("@/views/Favorites.vue"),
    meta: {
      title: "My Favorites",
      requiresAuth: true
    }
  },
  {
    path: "/admin",
    component: () => import("@/components/Sidebar.vue"),
    meta: {
      requiresAuth: true,
      adminOnly: true
    },
    children: [
      {
        path: "dashboard",
        name: "AdminDashboard",
        component: () => import("@/views/admin/Dashboard.vue"),
        meta: {
          title: "Admin Console",
          requiresAuth: true,
          adminOnly: true
        }
      },
      {
        path: "restaurants",
        name: "AdminRestaurants",
        component: () => import("@/views/admin/RestaurantManage.vue"), 
        meta: {
          title: "Restaurant Management",
          requiresAuth: true,
        }
      },
      {
        path: "tables",
        name: "AdminTables",
        component: () => import("@/views/admin/TableManage.vue"), 
        meta: {
          title: "Table Management",
          requiresAuth: true,
          adminOnly: true
        }
      },
      {
        path: "users",
        name: "AdminUsers",
        component: () => import("@/views/admin/UserManage.vue"),
        meta: {
          title: "User Management",
          requiresAuth: true,
          adminOnly: true
        }
      },
      {
        path: "reservations",
        name: "AdminReservations",
        component: () => import("@/views/admin/ReservationManage.vue"), 
        meta: {
          title: "Reservation Management",
          requiresAuth: true,
          adminOnly: true
        }
      },
      {
        path: "reviews",
        name: "AdminReviews",
        component: () => import("@/views/admin/ReviewManage.vue"), 
        meta: {
          title: "Review Management",
          requiresAuth: true,
          adminOnly: true
        }
      },
      {
        path: "staff",
        name: 'staff',
        component: ()=> import("@/views/admin/StaffManage.vue"),
        meta: {
          title: "Staff Review",
          adminOnly: true
        }
      },
      {
        path: "staff-apply",
        name: "StaffApply",
        component: () => import("@/views/staff/ApplyManager.vue"),
        meta: {
          title: "Apply for Staff",
          requiresAuth: true
        }
      },
      {
        path: "settings",
        name: "AdminSettings",
        component: () => import("@/views/admin/SystemSettings.vue"), 
        meta: {
          title: "System Settings",
          requiresAuth: true,
          adminOnly: true
        }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Set page title and handle authentication
router.beforeEach((to, from, next) => {
  // Set page title
  document.title = to.meta.title || "Restaurant Reservation";
  
  // If user is accessing the login page, allow direct access
  if (to.path === '/login') {
    return next();
  }
  
  // Get user information and role
  const userInfoStr = localStorage.getItem('userInfo');
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : {};
  const token = document.cookie.replace(/(?:(?:^|.*;\s*)token\s*\=\s*([^;]*).*$)|^.*$/, "$1");
  const userRole = userInfo?.role;
  
  // When user is not logged in, allow access to home page, otherwise redirect to login
  if (!token) {
    if (to.path === '/') {
      // Allow non-logged-in users to access home page
      return next();
    } else {
      // Redirect non-logged-in users to login page when accessing other authenticated pages
      return next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
    }
  }
  
  // Redirect logged-in users based on their role when accessing home page
  if (to.path === '/') {
    // Role 2 (regular user) stays on home page, other roles go to admin dashboard
    if (parseInt(userRole) === 2) {
      return next(); // Regular users stay on home page
    } else {
      // Admin(0), Restaurant Manager(1), Staff(3) redirect to admin dashboard
      return next({ path: '/admin/dashboard' });
    }
  }
  
  // Check if route requires authentication
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // Check if admin privileges are required
    const requiresAdmin = to.matched.some(record => record.meta.adminOnly);
    
    if (requiresAdmin && parseInt(userRole) === 2) {
      // If route requires admin privileges but user is regular user, redirect to home
      return next({ path: '/' });
    } else {
      // Permission verification passed, continue routing
      return next();
    }
  } else {
    return next();
  }
});

export default router;
