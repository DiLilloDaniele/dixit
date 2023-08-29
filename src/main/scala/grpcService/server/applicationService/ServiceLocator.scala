package grpcService.server.applicationService

import grpcService.server.data.adapters.AccessAdapter

object ServiceLocator:
  
  def getDataAdapter(): AccessAdapter = AccessAdapter(
    "localhost",
    "3306",
    "mysql",
    "DIXIT",
    "root",
    "root"
  )
