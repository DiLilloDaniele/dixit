package grpcService.server.applicationService

import grpcService.server.data.adapters.AccessAdapter

object ServiceLocator:
  
  def getDataAdapter(): AccessAdapter = AccessAdapter(
    "127.0.0.1",
    "6033",
    "mysql",
    "DIXIT",
    "user_name",
    "root_password"
  )
