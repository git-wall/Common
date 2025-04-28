package org.app.common.cache.ignite;

import lombok.Getter;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.ServiceResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceConfiguration;
import org.apache.ignite.services.ServiceDescriptor;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manager for Apache Ignite Service Grid - a distributed microservices' framework.
 * Provides methods for deploying, managing, and calling distributed services across the Ignite cluster.
 */
public class ServiceGridManager {

    /**
     * -- GETTER --
     *  Gets the underlying Ignite instance.
     *
     */
    @Getter
    private final Ignite ignite;
    private final IgniteProperties.ServiceGrid serviceGridConfig;
    private final Map<String, Object> deployedServices = new ConcurrentHashMap<>();

    public ServiceGridManager(Ignite ignite, IgniteProperties.ServiceGrid serviceGridConfig) {
        this.ignite = ignite;
        this.serviceGridConfig = serviceGridConfig;
    }

    /**
     * Deploys a singleton service across the entire cluster.
     *
     * @param name The service name
     * @param service The service implementation
     */
    public void deployClusterSingleton(String name, Service service) {
        ignite.services().deployClusterSingleton(name, service);
        deployedServices.put(name, service);
    }

    /**
     * Deploys a service on all nodes in the cluster.
     *
     * @param name The service name
     * @param service The service implementation
     * @param totalCount The total number of service instances in the cluster
     * @param maxPerNodeCount The maximum number of service instances per node
     */
    public void deployMultiple(
            String name,
            Service service,
            int totalCount,
            int maxPerNodeCount) {

        ignite.services().deployMultiple(name, service, totalCount, maxPerNodeCount);
        deployedServices.put(name, service);
    }

    /**
     * Deploys a service on a specific cluster group.
     *
     * @param name The service name
     * @param service The service implementation
     * @param clusterGroup The cluster group to deploy the service on
     */
    public void deploy(String name, Service service, ClusterGroup clusterGroup) {
        ignite.services(clusterGroup).deploy(new ServiceConfiguration()
                .setName(name)
                .setService(service)
                .setTotalCount(1)
                .setMaxPerNodeCount(1));

        deployedServices.put(name, service);
    }

    /**
     * Gets a proxy to the specified service.
     *
     * @param name The service name
     * @param serviceInterface The service interface
     * @param sticky Whether to use sticky mode (all calls go to the same service instance)
     * @param <T> The service interface type
     * @return The service proxy
     */
    public <T> T serviceProxy(String name, Class<T> serviceInterface, boolean sticky) {
        return ignite.services().serviceProxy(name, serviceInterface, sticky,
                serviceGridConfig.getServiceCallTimeout());
    }

    /**
     * Gets all service descriptors in the cluster.
     *
     * @return A collection of service descriptors
     */
    public Collection<ServiceDescriptor> serviceDescriptors() {
        return ignite.services().serviceDescriptors();
    }

    /**
     * Cancels (undeploys) a service by name.
     *
     * @param name The name of the service to cancel
     */
    public void cancelService(String name) {
        ignite.services().cancel(name);
        deployedServices.remove(name);
    }

    /**
     * Cancels all deployed services.
     */
    public void cancelAllServices() {
        for (String serviceName : deployedServices.keySet()) {
            ignite.services().cancel(serviceName);
        }
        deployedServices.clear();
    }

    /**
     * Checks if a service is deployed.
     *
     * @param name The service name
     * @return true if the service is deployed, false otherwise
     */
    public boolean isServiceDeployed(String name) {
        return ignite.services().serviceDescriptors().stream()
                .anyMatch(desc -> desc.name().equals(name));
    }

    /**
     * Executes a task with service injection across the cluster.
     *
     * @param serviceName The name of the service to inject
     * @param task The task to execute
     */
    public void executeWithServiceInjection(String serviceName, Runnable task) {
        ignite.compute().run(new ServiceInjectionTask(serviceName, task));
    }

    /**
     * Gets a list of all deployed service names.
     *
     * @return A collection of service names
     */
    public Collection<String> getDeployedServiceNames() {
        return deployedServices.keySet();
    }

    /**
      * Gets the node IDs where a specific service is deployed.
      *
      * @param serviceName The name of the service
      * @return A collection of node IDs
      */
     public Collection<UUID> getServiceNodeIds(String serviceName) {
         return ignite.services()
                 .serviceDescriptors()
                 .stream()
                 .filter(desc -> desc.name().equals(serviceName))
                 .flatMap(desc -> desc.topologySnapshot().keySet().stream())
                 .collect(Collectors.toList());
     }

    /**
     * Helper class for executing tasks with service injection.
     */
    private static class ServiceInjectionTask implements IgniteRunnable {
        private static final long serialVersionUID = 1L;

        @ServiceResource(serviceName = "dynamicServiceName", proxyInterface = Object.class)
        private transient Object srvc;

        private final String serviceName;
        private final Runnable task;

        public ServiceInjectionTask(String serviceName, Runnable task) {
            this.serviceName = serviceName;
            this.task = task;
        }

        @Override
        public void run() {
            try {
                // Map the dynamic service name to the actual service name at runtime
                System.setProperty("dynamicServiceName", serviceName);

                // Execute the task
                task.run();
            } catch (Exception e) {
                throw new IgniteException("Failed to execute service task", e);
            } finally {
                System.clearProperty("dynamicServiceName");
            }
        }
    }
}
