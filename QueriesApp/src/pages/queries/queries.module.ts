import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { QueriesPage } from './queries';

@NgModule({
  declarations: [
    QueriesPage,
  ],
  imports: [
    IonicPageModule.forChild(QueriesPage),
  ],
  exports: [
    QueriesPage
  ]
})
export class QueriesPageModule {}
